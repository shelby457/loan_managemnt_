package com.example.loan_management_api.config;

import com.example.loan_management_api.dto.LoanApprovalDTO;
import com.example.loan_management_api.dto.RepaymentRequestDTO;
import com.example.loan_management_api.model.Loan;
import com.example.loan_management_api.model.User;
import com.example.loan_management_api.model.enums.EmploymentStatus;
import com.example.loan_management_api.model.enums.LoanStatus;
import com.example.loan_management_api.model.enums.LoanType;
import com.example.loan_management_api.model.enums.PaymentMethod;
import com.example.loan_management_api.repository.LoanRepository;
import com.example.loan_management_api.repository.UserRepository;
import com.example.loan_management_api.service.EmiCalculatorService;
import com.example.loan_management_api.service.LoanService;
import com.example.loan_management_api.service.RepaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private LoanService loanService;

    @Autowired
    private RepaymentService repaymentService;

    @Autowired
    private EmiCalculatorService emiCalculatorService;

    @Autowired
    private com.example.loan_management_api.service.ExpenseService expenseService;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) return;

        // 1. Create Seed Borrowers
        User alex = userRepository.save(User.builder()
                .name("Alex Morgan")
                .email("alex.morgan@example.com")
                .phone("+1-555-0192")
                .monthlyIncome(8500.0)
                .creditScore(780)
                .employmentStatus(EmploymentStatus.EMPLOYED)
                .address("742 Evergreen Terrace, Springfield, OR")
                .build());

        User priya = userRepository.save(User.builder()
                .name("Priya Sharma")
                .email("priya.sharma@example.com")
                .phone("+1-555-0144")
                .monthlyIncome(12000.0)
                .creditScore(810)
                .employmentStatus(EmploymentStatus.BUSINESS_OWNER)
                .address("104 Tech Boulevard, Austin, TX")
                .build());

        User carlos = userRepository.save(User.builder()
                .name("Carlos Rodriguez")
                .email("carlos.r@example.com")
                .phone("+1-555-0189")
                .monthlyIncome(4500.0)
                .creditScore(640)
                .employmentStatus(EmploymentStatus.SELF_EMPLOYED)
                .address("512 Ocean Avenue, Miami, FL")
                .build());

        User emily = userRepository.save(User.builder()
                .name("Dr. Emily Watson")
                .email("emily.watson@example.com")
                .phone("+1-555-0163")
                .monthlyIncome(16000.0)
                .creditScore(795)
                .employmentStatus(EmploymentStatus.EMPLOYED)
                .address("88 Medical Park Way, Boston, MA")
                .build());

        // 2. Create Active Home Loan for Priya
        double homeLoanAmount = 450000.0;
        double homeLoanRate = 7.5;
        int homeLoanTerm = 120; // 10 years
        double homeEmi = emiCalculatorService.calculateMonthlyEmi(homeLoanAmount, homeLoanRate, homeLoanTerm);
        double homeTotalRepayable = EmiCalculatorService.round(homeEmi * homeLoanTerm);
        double homeTotalInterest = EmiCalculatorService.round(homeTotalRepayable - homeLoanAmount);

        Loan priyaLoan = Loan.builder()
                .user(priya)
                .loanType(LoanType.HOME)
                .principalAmount(homeLoanAmount)
                .interestRate(homeLoanRate)
                .termMonths(homeLoanTerm)
                .monthlyEmi(homeEmi)
                .totalInterest(homeTotalInterest)
                .totalRepayable(homeTotalRepayable)
                .remainingBalance(homeTotalRepayable)
                .purpose("Acquisition of luxury suburban condominium")
                .status(LoanStatus.PENDING)
                .appliedDate(LocalDateTime.now().minusMonths(6))
                .build();
        priyaLoan = loanRepository.save(priyaLoan);
        loanService.approveLoan(priyaLoan.getId(), LoanApprovalDTO.builder().approvedInterestRate(7.5).remarks("Prime borrower fast-track approval").build());

        // Make 2 payments for Priya's loan
        repaymentService.processRepayment(RepaymentRequestDTO.builder()
                .loanId(priyaLoan.getId())
                .amount(homeEmi)
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .notes("Month 1 auto-debit")
                .build());
        repaymentService.processRepayment(RepaymentRequestDTO.builder()
                .loanId(priyaLoan.getId())
                .amount(homeEmi)
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .notes("Month 2 auto-debit")
                .build());

        // 3. Create Active Auto Loan for Alex
        double autoLoanAmount = 35000.0;
        double autoRate = 8.2;
        int autoTerm = 36;
        double autoEmi = emiCalculatorService.calculateMonthlyEmi(autoLoanAmount, autoRate, autoTerm);
        double autoTotalRepayable = EmiCalculatorService.round(autoEmi * autoTerm);

        Loan alexLoan = Loan.builder()
                .user(alex)
                .loanType(LoanType.AUTO)
                .principalAmount(autoLoanAmount)
                .interestRate(autoRate)
                .termMonths(autoTerm)
                .monthlyEmi(autoEmi)
                .totalInterest(EmiCalculatorService.round(autoTotalRepayable - autoLoanAmount))
                .totalRepayable(autoTotalRepayable)
                .remainingBalance(autoTotalRepayable)
                .purpose("Purchase of Tesla Model 3 Electric Vehicle")
                .status(LoanStatus.PENDING)
                .appliedDate(LocalDateTime.now().minusMonths(2))
                .build();
        alexLoan = loanRepository.save(alexLoan);
        loanService.approveLoan(alexLoan.getId(), LoanApprovalDTO.builder().approvedInterestRate(8.0).remarks("Approved with 20 bps discount").build());

        // 4. Create Pending Loan for Emily (Doctor Practice Expansion)
        double busLoanAmount = 150000.0;
        double busRate = 11.5;
        int busTerm = 60;
        double busEmi = emiCalculatorService.calculateMonthlyEmi(busLoanAmount, busRate, busTerm);
        double busTotal = EmiCalculatorService.round(busEmi * busTerm);

        Loan emilyLoan = Loan.builder()
                .user(emily)
                .loanType(LoanType.BUSINESS)
                .principalAmount(busLoanAmount)
                .interestRate(busRate)
                .termMonths(busTerm)
                .monthlyEmi(busEmi)
                .totalInterest(EmiCalculatorService.round(busTotal - busLoanAmount))
                .totalRepayable(busTotal)
                .remainingBalance(busTotal)
                .purpose("Expansion of specialized pediatric clinic and MRI diagnostics setup")
                .status(LoanStatus.PENDING)
                .appliedDate(LocalDateTime.now().minusDays(1))
                .build();
        loanRepository.save(emilyLoan);

        // 5. Create Pending Personal Loan for Carlos
        double perAmount = 12000.0;
        double perRate = 13.5;
        int perTerm = 24;
        double perEmi = emiCalculatorService.calculateMonthlyEmi(perAmount, perRate, perTerm);
        double perTotal = EmiCalculatorService.round(perEmi * perTerm);

        Loan carlosLoan = Loan.builder()
                .user(carlos)
                .loanType(LoanType.PERSONAL)
                .principalAmount(perAmount)
                .interestRate(perRate)
                .termMonths(perTerm)
                .monthlyEmi(perEmi)
                .totalInterest(EmiCalculatorService.round(perTotal - perAmount))
                .totalRepayable(perTotal)
                .remainingBalance(perTotal)
                .purpose("Home renovation and energy efficiency solar panels")
                .status(LoanStatus.PENDING)
                .appliedDate(LocalDateTime.now().minusDays(3))
                .build();
        loanRepository.save(carlosLoan);

        // 6. Seed Monthly Expenses & Splitwise Group Splits
        seedExpensesAndSplits(alex, priya, carlos, emily);
    }

    private void seedExpensesAndSplits(User alex, User priya, User carlos, User emily) {
        // Expense 1: Office Rent ($1,200) Paid by Priya, Split equally 4 ways ($300 each)
        expenseService.createExpense(com.example.loan_management_api.dto.ExpenseRequestDTO.builder()
                .description("Co-working Office Rent & Dedicated Desks")
                .amount(1200.0)
                .category(com.example.loan_management_api.model.enums.ExpenseCategory.HOUSING)
                .expenseDate(java.time.LocalDate.now().minusDays(5))
                .payerId(priya.getId())
                .splitType(com.example.loan_management_api.model.enums.SplitType.EQUAL)
                .isSplit(true)
                .notes("Monthly lease for tech startup hub")
                .splits(java.util.List.of(
                        new com.example.loan_management_api.dto.ExpenseSplitRequestDTO(priya.getId(), 300.0, 25.0),
                        new com.example.loan_management_api.dto.ExpenseSplitRequestDTO(alex.getId(), 300.0, 25.0),
                        new com.example.loan_management_api.dto.ExpenseSplitRequestDTO(carlos.getId(), 300.0, 25.0),
                        new com.example.loan_management_api.dto.ExpenseSplitRequestDTO(emily.getId(), 300.0, 25.0)
                ))
                .build());

        // Expense 2: Team Dinner ($280) Paid by Alex, Split 4 ways ($70 each)
        expenseService.createExpense(com.example.loan_management_api.dto.ExpenseRequestDTO.builder()
                .description("Quarterly Team Celebration Dinner at Ocean Grill")
                .amount(280.0)
                .category(com.example.loan_management_api.model.enums.ExpenseCategory.FOOD_DINING)
                .expenseDate(java.time.LocalDate.now().minusDays(3))
                .payerId(alex.getId())
                .splitType(com.example.loan_management_api.model.enums.SplitType.EQUAL)
                .isSplit(true)
                .notes("Celebration of new branch launch")
                .splits(java.util.List.of(
                        new com.example.loan_management_api.dto.ExpenseSplitRequestDTO(alex.getId(), 70.0, 25.0),
                        new com.example.loan_management_api.dto.ExpenseSplitRequestDTO(priya.getId(), 70.0, 25.0),
                        new com.example.loan_management_api.dto.ExpenseSplitRequestDTO(carlos.getId(), 70.0, 25.0),
                        new com.example.loan_management_api.dto.ExpenseSplitRequestDTO(emily.getId(), 70.0, 25.0)
                ))
                .build());

        // Expense 3: Cloud Hosting & AWS Servers ($450) Paid by Priya (Personal/Company expense)
        expenseService.createExpense(com.example.loan_management_api.dto.ExpenseRequestDTO.builder()
                .description("AWS Cloud Hosting & Database Cluster")
                .amount(450.0)
                .category(com.example.loan_management_api.model.enums.ExpenseCategory.OFFICE_BUSINESS)
                .expenseDate(java.time.LocalDate.now().minusDays(2))
                .payerId(priya.getId())
                .isSplit(false)
                .notes("Production cloud infrastructure")
                .build());

        // Expense 4: Fiber Internet & Utilities ($160) Paid by Emily, Split 4 ways ($40 each)
        expenseService.createExpense(com.example.loan_management_api.dto.ExpenseRequestDTO.builder()
                .description("Gigabit Fiber Internet & Power Utilities")
                .amount(160.0)
                .category(com.example.loan_management_api.model.enums.ExpenseCategory.UTILITIES)
                .expenseDate(java.time.LocalDate.now().minusDays(1))
                .payerId(emily.getId())
                .splitType(com.example.loan_management_api.model.enums.SplitType.EQUAL)
                .isSplit(true)
                .notes("Office high-speed connectivity")
                .splits(java.util.List.of(
                        new com.example.loan_management_api.dto.ExpenseSplitRequestDTO(emily.getId(), 40.0, 25.0),
                        new com.example.loan_management_api.dto.ExpenseSplitRequestDTO(alex.getId(), 40.0, 25.0),
                        new com.example.loan_management_api.dto.ExpenseSplitRequestDTO(priya.getId(), 40.0, 25.0),
                        new com.example.loan_management_api.dto.ExpenseSplitRequestDTO(carlos.getId(), 40.0, 25.0)
                ))
                .build());

        // Expense 5: Grocery & Coffee Supplies ($120) Paid by Carlos, Split between Carlos and Alex ($60 each)
        expenseService.createExpense(com.example.loan_management_api.dto.ExpenseRequestDTO.builder()
                .description("Artisan Coffee Beans & Pantry Snacks")
                .amount(120.0)
                .category(com.example.loan_management_api.model.enums.ExpenseCategory.FOOD_DINING)
                .expenseDate(java.time.LocalDate.now())
                .payerId(carlos.getId())
                .splitType(com.example.loan_management_api.model.enums.SplitType.EQUAL)
                .isSplit(true)
                .splits(java.util.List.of(
                        new com.example.loan_management_api.dto.ExpenseSplitRequestDTO(carlos.getId(), 60.0, 50.0),
                        new com.example.loan_management_api.dto.ExpenseSplitRequestDTO(alex.getId(), 60.0, 50.0)
                ))
                .build());

        // Record a partial settlement: Carlos paid Priya $100 via UPI
        expenseService.recordSettlement(com.example.loan_management_api.dto.SettlementRequestDTO.builder()
                .payerId(carlos.getId())
                .payeeId(priya.getId())
                .amount(100.0)
                .paymentMethod(com.example.loan_management_api.model.enums.PaymentMethod.UPI)
                .notes("Partial rent payment via UPI")
                .build());
    }
}
