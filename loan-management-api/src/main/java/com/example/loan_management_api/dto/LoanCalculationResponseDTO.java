package com.example.loan_management_api.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanCalculationResponseDTO {
    private Double principalAmount;
    private Double annualInterestRate;
    private Integer termMonths;
    private Double monthlyEmi;
    private Double totalInterest;
    private Double totalRepayable;
    private Double interestPercentageOfTotal;
    private List<EmiScheduleDTO> amortizationSchedule;
}
