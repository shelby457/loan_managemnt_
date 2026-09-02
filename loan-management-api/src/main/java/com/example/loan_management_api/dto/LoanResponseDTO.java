package com.example.loan_management_api.dto;

import com.example.loan_management_api.model.enums.LoanStatus;
import com.example.loan_management_api.model.enums.LoanType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanResponseDTO {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Integer userCreditScore;
    private LoanType loanType;
    private Double principalAmount;
    private Double interestRate;
    private Integer termMonths;
    private Double monthlyEmi;
    private Double totalInterest;
    private Double totalRepayable;
    private Double remainingBalance;
    private Double totalPaid;
    private Integer paidInstallments;
    private Integer remainingInstallments;
    private String purpose;
    private LoanStatus status;
    private String rejectionReason;
    private LocalDateTime appliedDate;
    private LocalDateTime approvedDate;
    private LocalDateTime closedDate;
    private List<EmiScheduleDTO> emiSchedules;
    private List<RepaymentResponseDTO> recentRepayments;
}
