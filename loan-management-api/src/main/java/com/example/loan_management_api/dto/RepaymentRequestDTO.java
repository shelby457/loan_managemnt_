package com.example.loan_management_api.dto;

import com.example.loan_management_api.model.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepaymentRequestDTO {

    @NotNull(message = "Loan ID is required")
    private Long loanId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Payment amount must be greater than 0")
    private Double amount;

    private PaymentMethod paymentMethod;

    private String notes;
}
