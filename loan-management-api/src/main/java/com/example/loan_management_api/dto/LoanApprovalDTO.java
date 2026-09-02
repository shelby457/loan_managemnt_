package com.example.loan_management_api.dto;

import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApprovalDTO {
    @Positive(message = "Interest rate override must be positive")
    private Double approvedInterestRate; // Optional override

    private String remarks;
}
