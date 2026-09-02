package com.example.loan_management_api.dto;

import com.example.loan_management_api.model.enums.LoanType;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplicationDTO {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Loan type is required")
    private LoanType loanType;

    @NotNull(message = "Principal amount is required")
    @Positive(message = "Principal amount must be greater than 0")
    @Min(value = 1000, message = "Minimum loan amount is 1000")
    private Double principalAmount;

    @Positive(message = "Interest rate must be greater than 0")
    private Double customInterestRate; // Optional, defaults to LoanType standard

    @NotNull(message = "Term in months is required")
    @Min(value = 1, message = "Minimum term is 1 month")
    @Max(value = 360, message = "Maximum term is 360 months")
    private Integer termMonths;

    private String purpose;
}
