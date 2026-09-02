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
public class SettlementRequestDTO {

    @NotNull(message = "Payer ID (debtor) is required")
    private Long payerId;

    @NotNull(message = "Payee ID (creditor) is required")
    private Long payeeId;

    @NotNull(message = "Settlement amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;

    private PaymentMethod paymentMethod;

    private String notes;
}
