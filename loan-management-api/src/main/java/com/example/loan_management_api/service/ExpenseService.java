package com.example.loan_management_api.service;

import com.example.loan_management_api.dto.*;
import com.example.loan_management_api.exception.BadRequestException;
import com.example.loan_management_api.exception.ResourceNotFoundException;
import com.example.loan_management_api.model.Expense;
import com.example.loan_management_api.model.ExpenseSplit;
import com.example.loan_management_api.model.Settlement;
import com.example.loan_management_api.model.User;
import com.example.loan_management_api.model.enums.ExpenseCategory;
import com.example.loan_management_api.model.enums.PaymentMethod;
import com.example.loan_management_api.model.enums.SplitType;
import com.example.loan_management_api.repository.ExpenseRepository;
import com.example.loan_management_api.repository.ExpenseSplitRepository;
import com.example.loan_management_api.repository.SettlementRepository;
import com.example.loan_management_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ExpenseSplitRepository expenseSplitRepository;

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public ExpenseResponseDTO createExpense(ExpenseRequestDTO request) {
        User payer = userService.getUserEntity(request.getPayerId());

        LocalDate date = request.getExpenseDate() != null ? request.getExpenseDate() : LocalDate.now();
        ExpenseCategory category = request.getCategory() != null ? request.getCategory() : ExpenseCategory.MISCELLANEOUS;
        SplitType splitType = request.getSplitType() != null ? request.getSplitType() : SplitType.EQUAL;
        boolean isSplit = Boolean.TRUE.equals(request.getIsSplit()) && request.getSplits() != null && !request.getSplits().isEmpty();

        Expense expense = Expense.builder()
                .description(request.getDescription())
                .amount(EmiCalculatorService.round(request.getAmount()))
                .category(category)
                .expenseDate(date)
                .payer(payer)
                .splitType(splitType)
                .notes(request.getNotes())
                .isSplit(isSplit)
                .createdAt(LocalDateTime.now())
                .build();

        if (isSplit) {
            List<ExpenseSplit> splitEntities = new ArrayList<>();
            List<ExpenseSplitRequestDTO> splitReqs = request.getSplits();

            if (splitType == SplitType.EQUAL) {
                // Total participants including or excluding payer
                int totalPeople = splitReqs.size();
                double share = EmiCalculatorService.round(request.getAmount() / totalPeople);

                for (ExpenseSplitRequestDTO sr : splitReqs) {
                    if (sr.getUserId().equals(payer.getId())) {
                        continue; // Payer's own share is not owed to anyone
                    }
                    User participant = userService.getUserEntity(sr.getUserId());
                    splitEntities.add(ExpenseSplit.builder()
                            .expense(expense)
                            .user(participant)
                            .owedAmount(share)
                            .percentage(EmiCalculatorService.round(100.0 / totalPeople))
                            .settled(false)
                            .build());
                }
            } else if (splitType == SplitType.EXACT_AMOUNT) {
                for (ExpenseSplitRequestDTO sr : splitReqs) {
                    if (sr.getUserId().equals(payer.getId())) continue;
                    if (sr.getOwedAmount() == null || sr.getOwedAmount() <= 0) {
                        throw new BadRequestException("Owed amount must be provided for exact split");
                    }
                    User participant = userService.getUserEntity(sr.getUserId());
                    splitEntities.add(ExpenseSplit.builder()
                            .expense(expense)
                            .user(participant)
                            .owedAmount(EmiCalculatorService.round(sr.getOwedAmount()))
                            .settled(false)
                            .build());
                }
            } else if (splitType == SplitType.PERCENTAGE) {
                for (ExpenseSplitRequestDTO sr : splitReqs) {
                    if (sr.getUserId().equals(payer.getId())) continue;
                    double pct = (sr.getPercentage() != null) ? sr.getPercentage() : 0.0;
                    double owed = EmiCalculatorService.round(request.getAmount() * (pct / 100.0));
                    User participant = userService.getUserEntity(sr.getUserId());
                    splitEntities.add(ExpenseSplit.builder()
                            .expense(expense)
                            .user(participant)
                            .owedAmount(owed)
                            .percentage(pct)
                            .settled(false)
                            .build());
                }
            }

            expense.setSplits(splitEntities);
        }

        Expense saved = expenseRepository.save(expense);
        return mapToDTO(saved);
    }

    public List<ExpenseResponseDTO> getAllExpenses(Integer year, Integer month, ExpenseCategory category, Long payerId) {
        List<Expense> list = expenseRepository.findAll();

        if (year != null && month != null) {
            YearMonth ym = YearMonth.of(year, month);
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();
            list = list.stream()
                    .filter(e -> !e.getExpenseDate().isBefore(start) && !e.getExpenseDate().isAfter(end))
                    .collect(Collectors.toList());
        }

        if (category != null) {
            list = list.stream().filter(e -> e.getCategory() == category).collect(Collectors.toList());
        }

        if (payerId != null) {
            list = list.stream().filter(e -> e.getPayer().getId().equals(payerId)).collect(Collectors.toList());
        }

        list.sort((a, b) -> b.getExpenseDate().compareTo(a.getExpenseDate()));
        return list.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public ExpenseResponseDTO getExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
        return mapToDTO(expense);
    }

    @Transactional
    public void deleteExpense(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
        expenseRepository.delete(expense);
    }

    public MonthlyExpenseSummaryDTO getMonthlySummary(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<Expense> monthExpenses = expenseRepository.findByExpenseDateBetweenOrderByExpenseDateDesc(start, end);

        double totalSpent = 0.0;
        Map<String, Double> byCategory = new HashMap<>();
        Map<String, Double> byPayer = new HashMap<>();

        for (Expense e : monthExpenses) {
            totalSpent += e.getAmount();
            String catName = e.getCategory().name();
            byCategory.put(catName, EmiCalculatorService.round(byCategory.getOrDefault(catName, 0.0) + e.getAmount()));

            String payerName = e.getPayer().getName();
            byPayer.put(payerName, EmiCalculatorService.round(byPayer.getOrDefault(payerName, 0.0) + e.getAmount()));
        }

        int count = monthExpenses.size();
        double avg = count > 0 ? EmiCalculatorService.round(totalSpent / count) : 0.0;

        return MonthlyExpenseSummaryDTO.builder()
                .year(year)
                .month(month)
                .monthName(ym.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                .totalSpent(EmiCalculatorService.round(totalSpent))
                .totalExpenseCount(count)
                .averageExpense(avg)
                .expensesByCategory(byCategory)
                .expensesByPayer(byPayer)
                .build();
    }

    /**
     * Splitwise Core Engine: Computes pairwise net balances and debt graph
     */
    public SplitwiseBoardDTO getSplitwiseBoard() {
        List<User> allUsers = userRepository.findAll();
        List<Expense> allExpenses = expenseRepository.findAll();
        List<Settlement> allSettlements = settlementRepository.findAll();

        // Pairwise net debt matrix: debtMatrix[debteeId][debtorId] = amount debtor owes debtee
        Map<Long, Map<Long, Double>> debtMatrix = new HashMap<>();
        for (User u : allUsers) {
            debtMatrix.put(u.getId(), new HashMap<>());
        }

        double totalGroupSpending = 0.0;

        // 1. Process Expense Splits
        for (Expense e : allExpenses) {
            if (Boolean.TRUE.equals(e.getIsSplit()) && e.getSplits() != null) {
                totalGroupSpending += e.getAmount();
                Long payerId = e.getPayer().getId();

                for (ExpenseSplit split : e.getSplits()) {
                    if (Boolean.FALSE.equals(split.getSettled())) {
                        Long debtorId = split.getUser().getId();
                        if (!debtorId.equals(payerId)) {
                            double cur = debtMatrix.getOrDefault(payerId, new HashMap<>()).getOrDefault(debtorId, 0.0);
                            debtMatrix.computeIfAbsent(payerId, k -> new HashMap<>()).put(debtorId, cur + split.getOwedAmount());
                        }
                    }
                }
            }
        }

        // 2. Adjust for Settlements
        for (Settlement s : allSettlements) {
            Long debtorId = s.getPayer().getId();
            Long creditorId = s.getPayee().getId();
            double cur = debtMatrix.getOrDefault(creditorId, new HashMap<>()).getOrDefault(debtorId, 0.0);
            debtMatrix.computeIfAbsent(creditorId, k -> new HashMap<>()).put(debtorId, Math.max(0.0, cur - s.getAmount()));
        }

        // 3. Simplify Pairwise Debts: if A owes B $50 and B owes A $20 -> A owes B $30
        List<PeerDebtDTO> simplifiedDebts = new ArrayList<>();
        Map<Long, String> userNames = allUsers.stream().collect(Collectors.toMap(User::getId, User::getName));
        double totalUnsettled = 0.0;

        for (int i = 0; i < allUsers.size(); i++) {
            for (int j = i + 1; j < allUsers.size(); j++) {
                Long u1 = allUsers.get(i).getId();
                Long u2 = allUsers.get(j).getId();

                double u2OwesU1 = debtMatrix.getOrDefault(u1, Collections.emptyMap()).getOrDefault(u2, 0.0);
                double u1OwesU2 = debtMatrix.getOrDefault(u2, Collections.emptyMap()).getOrDefault(u1, 0.0);

                double net = EmiCalculatorService.round(u2OwesU1 - u1OwesU2);

                if (net > 0.01) {
                    // u2 owes u1 'net'
                    simplifiedDebts.add(PeerDebtDTO.builder()
                            .debtorId(u2)
                            .debtorName(userNames.get(u2))
                            .creditorId(u1)
                            .creditorName(userNames.get(u1))
                            .amount(net)
                            .summaryText(userNames.get(u2) + " owes " + userNames.get(u1) + " $" + String.format("%.2f", net))
                            .build());
                    totalUnsettled += net;
                } else if (net < -0.01) {
                    // u1 owes u2 '|net|'
                    double absNet = Math.abs(net);
                    simplifiedDebts.add(PeerDebtDTO.builder()
                            .debtorId(u1)
                            .debtorName(userNames.get(u1))
                            .creditorId(u2)
                            .creditorName(userNames.get(u2))
                            .amount(absNet)
                            .summaryText(userNames.get(u1) + " owes " + userNames.get(u2) + " $" + String.format("%.2f", absNet))
                            .build());
                    totalUnsettled += absNet;
                }
            }
        }

        // 4. Compute Net Balance per User
        List<UserBalanceDTO> userBalances = new ArrayList<>();
        for (User u : allUsers) {
            double getsBack = 0.0;
            double owes = 0.0;

            for (PeerDebtDTO d : simplifiedDebts) {
                if (d.getCreditorId().equals(u.getId())) {
                    getsBack += d.getAmount();
                }
                if (d.getDebtorId().equals(u.getId())) {
                    owes += d.getAmount();
                }
            }

            double net = EmiCalculatorService.round(getsBack - owes);
            String status = "SETTLED";
            if (net > 0.01) status = "OWED";
            else if (net < -0.01) status = "OWES";

            userBalances.add(UserBalanceDTO.builder()
                    .userId(u.getId())
                    .userName(u.getName())
                    .userEmail(u.getEmail())
                    .totalPaidForGroup(EmiCalculatorService.round(getsBack))
                    .totalOwedToGroup(EmiCalculatorService.round(owes))
                    .netBalance(net)
                    .status(status)
                    .build());
        }

        return SplitwiseBoardDTO.builder()
                .totalGroupSpending(EmiCalculatorService.round(totalGroupSpending))
                .totalUnsettledDebt(EmiCalculatorService.round(totalUnsettled))
                .userBalances(userBalances)
                .simplifiedDebts(simplifiedDebts)
                .build();
    }

    @Transactional
    public SettlementResponseDTO recordSettlement(SettlementRequestDTO request) {
        if (request.getPayerId().equals(request.getPayeeId())) {
            throw new BadRequestException("Payer and Payee cannot be the same user");
        }

        User payer = userService.getUserEntity(request.getPayerId());
        User payee = userService.getUserEntity(request.getPayeeId());

        String txRef = "SETTLE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Settlement settlement = Settlement.builder()
                .payer(payer)
                .payee(payee)
                .amount(EmiCalculatorService.round(request.getAmount()))
                .settlementDate(LocalDateTime.now())
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.UPI)
                .transactionReference(txRef)
                .notes(request.getNotes())
                .build();

        Settlement saved = settlementRepository.save(settlement);

        return SettlementResponseDTO.builder()
                .id(saved.getId())
                .payerId(payer.getId())
                .payerName(payer.getName())
                .payeeId(payee.getId())
                .payeeName(payee.getName())
                .amount(saved.getAmount())
                .settlementDate(saved.getSettlementDate())
                .paymentMethod(saved.getPaymentMethod())
                .transactionReference(saved.getTransactionReference())
                .notes(saved.getNotes())
                .build();
    }

    public List<SettlementResponseDTO> getAllSettlements() {
        return settlementRepository.findAllByOrderBySettlementDateDesc().stream()
                .map(s -> SettlementResponseDTO.builder()
                        .id(s.getId())
                        .payerId(s.getPayer().getId())
                        .payerName(s.getPayer().getName())
                        .payeeId(s.getPayee().getId())
                        .payeeName(s.getPayee().getName())
                        .amount(s.getAmount())
                        .settlementDate(s.getSettlementDate())
                        .paymentMethod(s.getPaymentMethod())
                        .transactionReference(s.getTransactionReference())
                        .notes(s.getNotes())
                        .build())
                .collect(Collectors.toList());
    }

    public ExpenseResponseDTO mapToDTO(Expense expense) {
        List<ExpenseSplitResponseDTO> splits = (expense.getSplits() != null) ? expense.getSplits().stream()
                .map(s -> ExpenseSplitResponseDTO.builder()
                        .id(s.getId())
                        .userId(s.getUser().getId())
                        .userName(s.getUser().getName())
                        .userEmail(s.getUser().getEmail())
                        .owedAmount(s.getOwedAmount())
                        .percentage(s.getPercentage())
                        .settled(s.getSettled())
                        .settledDate(s.getSettledDate())
                        .build())
                .collect(Collectors.toList()) : List.of();

        return ExpenseResponseDTO.builder()
                .id(expense.getId())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .category(expense.getCategory())
                .expenseDate(expense.getExpenseDate())
                .payerId(expense.getPayer().getId())
                .payerName(expense.getPayer().getName())
                .payerEmail(expense.getPayer().getEmail())
                .splitType(expense.getSplitType())
                .notes(expense.getNotes())
                .isSplit(expense.getIsSplit())
                .splits(splits)
                .createdAt(expense.getCreatedAt())
                .build();
    }
}
