package com.example.loan_management_api.service;

import com.example.loan_management_api.dto.*;
import com.example.loan_management_api.exception.InvalidLoanOperationException;
import com.example.loan_management_api.exception.ResourceNotFoundException;
import com.example.loan_management_api.model.EmiSchedule;
import com.example.loan_management_api.model.Loan;
import com.example.loan_management_api.model.User;
import com.example.loan_management_api.model.enums.LoanStatus;
import com.example.loan_management_api.model.enums.RepaymentStatus;
import com.example.loan_management_api.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private EmiCalculatorService emiCalculatorService;

    @Autowired
    private CreditUnderwritingService underwritingService;

    @Transactional
    public LoanResponseDTO applyLoan(LoanApplicationDTO request) {
        User user = userService.getUserEntity(request.getUserId());

        double interestRate = request.getCustomInterestRate() != null && request.getCustomInterestRate() > 0
                ? request.getCustomInterestRate()
                : request.getLoanType().getDefaultInterestRate();

        double monthlyEmi = emiCalculatorService.calculateMonthlyEmi(
                request.getPrincipalAmount(),
                interestRate,
                request.getTermMonths()
        );

        double totalRepayable = EmiCalculatorService.round(monthlyEmi * request.getTermMonths());
        double totalInterest = EmiCalculatorService.round(totalRepayable - request.getPrincipalAmount());

        Loan loan = Loan.builder()
                .user(user)
                .loanType(request.getLoanType())
                .principalAmount(request.getPrincipalAmount())
                .interestRate(interestRate)
                .termMonths(request.getTermMonths())
                .monthlyEmi(monthlyEmi)
                .totalInterest(totalInterest)
                .totalRepayable(totalRepayable)
                .remainingBalance(totalRepayable)
                .purpose(request.getPurpose())
                .status(LoanStatus.PENDING)
                .appliedDate(LocalDateTime.now())
                .build();

        Loan saved = loanRepository.save(loan);
        return mapToDTO(saved);
    }

    @Transactional
    public LoanResponseDTO approveLoan(Long loanId, LoanApprovalDTO approvalDTO) {
        Loan loan = getLoanEntity(loanId);

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new InvalidLoanOperationException("Only PENDING loans can be approved. Current status: " + loan.getStatus());
        }

        if (approvalDTO != null && approvalDTO.getApprovedInterestRate() != null && approvalDTO.getApprovedInterestRate() > 0) {
            loan.setInterestRate(approvalDTO.getApprovedInterestRate());
            double newEmi = emiCalculatorService.calculateMonthlyEmi(
                    loan.getPrincipalAmount(),
                    loan.getInterestRate(),
                    loan.getTermMonths()
            );
            loan.setMonthlyEmi(newEmi);
            loan.setTotalRepayable(EmiCalculatorService.round(newEmi * loan.getTermMonths()));
            loan.setTotalInterest(EmiCalculatorService.round(loan.getTotalRepayable() - loan.getPrincipalAmount()));
            loan.setRemainingBalance(loan.getTotalRepayable());
        }

        loan.setStatus(LoanStatus.ACTIVE);
        loan.setApprovedDate(LocalDateTime.now());

        // Generate EMI Schedule
        List<EmiSchedule> schedules = emiCalculatorService.generateEntitySchedule(loan, LocalDate.now().plusMonths(1));
        loan.getEmiSchedules().clear();
        loan.getEmiSchedules().addAll(schedules);

        Loan saved = loanRepository.save(loan);
        return mapToDTO(saved);
    }

    @Transactional
    public LoanResponseDTO rejectLoan(Long loanId, LoanRejectionDTO rejectionDTO) {
        Loan loan = getLoanEntity(loanId);

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new InvalidLoanOperationException("Only PENDING loans can be rejected. Current status: " + loan.getStatus());
        }

        loan.setStatus(LoanStatus.REJECTED);
        loan.setRejectionReason(rejectionDTO != null ? rejectionDTO.getReason() : "Did not meet underwriting criteria");

        Loan saved = loanRepository.save(loan);
        return mapToDTO(saved);
    }

    public Loan getLoanEntity(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + id));
    }

    public LoanResponseDTO getLoanById(Long id) {
        Loan loan = getLoanEntity(id);
        return mapToDTO(loan);
    }

    public List<LoanResponseDTO> getAllLoans(LoanStatus status) {
        List<Loan> list = (status != null) ? loanRepository.findByStatus(status) : loanRepository.findAll();
        return list.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<LoanResponseDTO> getLoansByUserId(Long userId, LoanStatus status) {
        List<Loan> list = (status != null)
                ? loanRepository.findByUserIdAndStatus(userId, status)
                : loanRepository.findByUserId(userId);
        return list.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public CreditEvaluationDTO evaluateLoanEligibility(Long userId, Double principalAmount, Integer termMonths) {
        User user = userService.getUserEntity(userId);
        double rate = 10.0;
        double emi = emiCalculatorService.calculateMonthlyEmi(principalAmount, rate, termMonths);
        return underwritingService.evaluateEligibility(user, principalAmount, emi);
    }

    public LoanResponseDTO mapToDTO(Loan loan) {
        List<EmiScheduleDTO> emiDTOs = loan.getEmiSchedules() != null ? loan.getEmiSchedules().stream()
                .map(s -> EmiScheduleDTO.builder()
                        .id(s.getId())
                        .installmentNumber(s.getInstallmentNumber())
                        .dueDate(s.getDueDate())
                        .emiAmount(s.getEmiAmount())
                        .principalComponent(s.getPrincipalComponent())
                        .interestComponent(s.getInterestComponent())
                        .remainingPrincipal(s.getRemainingPrincipal())
                        .status(s.getStatus())
                        .paidDate(s.getPaidDate())
                        .build())
                .collect(Collectors.toList()) : List.of();

        List<RepaymentResponseDTO> repaymentDTOs = loan.getRepayments() != null ? loan.getRepayments().stream()
                .map(r -> RepaymentResponseDTO.builder()
                        .id(r.getId())
                        .loanId(loan.getId())
                        .installmentNumber(r.getInstallmentNumber())
                        .amountPaid(r.getAmountPaid())
                        .paymentDate(r.getPaymentDate())
                        .paymentMethod(r.getPaymentMethod())
                        .transactionReference(r.getTransactionReference())
                        .notes(r.getNotes())
                        .remainingLoanBalance(loan.getRemainingBalance())
                        .build())
                .collect(Collectors.toList()) : List.of();

        int paidCount = 0;
        if (loan.getEmiSchedules() != null) {
            for (EmiSchedule s : loan.getEmiSchedules()) {
                if (s.getStatus() == RepaymentStatus.PAID) paidCount++;
            }
        }
        int totalInstallments = loan.getTermMonths() != null ? loan.getTermMonths() : 0;
        int remainingInstallments = Math.max(0, totalInstallments - paidCount);

        double totalPaid = (loan.getTotalRepayable() != null && loan.getRemainingBalance() != null)
                ? EmiCalculatorService.round(loan.getTotalRepayable() - loan.getRemainingBalance())
                : 0.0;

        return LoanResponseDTO.builder()
                .id(loan.getId())
                .userId(loan.getUser().getId())
                .userName(loan.getUser().getName())
                .userEmail(loan.getUser().getEmail())
                .userCreditScore(loan.getUser().getCreditScore())
                .loanType(loan.getLoanType())
                .principalAmount(loan.getPrincipalAmount())
                .interestRate(loan.getInterestRate())
                .termMonths(loan.getTermMonths())
                .monthlyEmi(loan.getMonthlyEmi())
                .totalInterest(loan.getTotalInterest())
                .totalRepayable(loan.getTotalRepayable())
                .remainingBalance(loan.getRemainingBalance())
                .totalPaid(totalPaid)
                .paidInstallments(paidCount)
                .remainingInstallments(remainingInstallments)
                .purpose(loan.getPurpose())
                .status(loan.getStatus())
                .rejectionReason(loan.getRejectionReason())
                .appliedDate(loan.getAppliedDate())
                .approvedDate(loan.getApprovedDate())
                .closedDate(loan.getClosedDate())
                .emiSchedules(emiDTOs)
                .recentRepayments(repaymentDTOs)
                .build();
    }
}