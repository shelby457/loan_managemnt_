package com.example.loan_management_api.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditEvaluationDTO {
    private boolean eligible;
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
    private Integer creditScore;
    private String creditRating;
    private Double estimatedMaxLoanAmount;
    private Double debtToIncomeRatio;
    private List<String> underwritingNotes;
}
