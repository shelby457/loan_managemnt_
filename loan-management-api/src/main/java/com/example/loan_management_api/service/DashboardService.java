package com.example.loan_management_api.service;

import com.example.loan_management_api.dto.DashboardStatsDTO;
import com.example.loan_management_api.model.Loan;
import com.example.loan_management_api.model.enums.LoanStatus;
import com.example.loan_management_api.repository.LoanRepository;
import com.example.loan_management_api.repository.RepaymentRepository;
import com.example.loan_management_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private RepaymentRepository repaymentRepository;

    public DashboardStatsDTO getDashboardStats() {
        long totalBorrowers = userRepository.count();
        long totalLoans = loanRepository.count();
        long pendingLoans = loanRepository.countByStatus(LoanStatus.PENDING);
        long approvedLoans = loanRepository.countByStatus(LoanStatus.APPROVED);
        long activeLoans = loanRepository.countByStatus(LoanStatus.ACTIVE);
        long fullyPaidLoans = loanRepository.countByStatus(LoanStatus.FULLY_PAID);
        long rejectedLoans = loanRepository.countByStatus(LoanStatus.REJECTED);

        Double totalDisbursed = loanRepository.sumTotalDisbursedAmount();
        Double totalRepayments = repaymentRepository.sumTotalRepaymentsCollected();
        Double totalRemaining = loanRepository.sumActiveRemainingBalance();
        Double totalInterest = loanRepository.sumTotalProjectedInterest();

        long evaluatedLoans = totalLoans - pendingLoans;
        double approvalRate = (evaluatedLoans > 0)
                ? EmiCalculatorService.round(((double) (approvedLoans + activeLoans + fullyPaidLoans) / evaluatedLoans) * 100.0)
                : 100.0;

        List<Loan> allLoans = loanRepository.findAll();
        Map<String, Long> loansByType = new HashMap<>();
        Map<String, Long> loansByStatus = new HashMap<>();

        for (Loan l : allLoans) {
            String typeName = l.getLoanType().name();
            loansByType.put(typeName, loansByType.getOrDefault(typeName, 0L) + 1);

            String statusName = l.getStatus().name();
            loansByStatus.put(statusName, loansByStatus.getOrDefault(statusName, 0L) + 1);
        }

        return DashboardStatsDTO.builder()
                .totalBorrowers(totalBorrowers)
                .totalLoans(totalLoans)
                .pendingLoans(pendingLoans)
                .approvedLoans(approvedLoans)
                .activeLoans(activeLoans)
                .fullyPaidLoans(fullyPaidLoans)
                .rejectedLoans(rejectedLoans)
                .totalDisbursedAmount(EmiCalculatorService.round(totalDisbursed != null ? totalDisbursed : 0.0))
                .totalRepaymentsCollected(EmiCalculatorService.round(totalRepayments != null ? totalRepayments : 0.0))
                .totalActiveRemainingBalance(EmiCalculatorService.round(totalRemaining != null ? totalRemaining : 0.0))
                .totalProjectedInterest(EmiCalculatorService.round(totalInterest != null ? totalInterest : 0.0))
                .approvalRate(approvalRate)
                .loansByType(loansByType)
                .loansByStatus(loansByStatus)
                .build();
    }
}
