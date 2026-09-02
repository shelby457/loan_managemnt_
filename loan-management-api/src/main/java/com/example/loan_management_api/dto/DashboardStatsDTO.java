package com.example.loan_management_api.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDTO {
    private long totalBorrowers;
    private long totalLoans;
    private long pendingLoans;
    private long approvedLoans;
    private long activeLoans;
    private long fullyPaidLoans;
    private long rejectedLoans;
    private Double totalDisbursedAmount;
    private Double totalRepaymentsCollected;
    private Double totalActiveRemainingBalance;
    private Double totalProjectedInterest;
    private Double approvalRate;
    private Map<String, Long> loansByType;
    private Map<String, Long> loansByStatus;
}
