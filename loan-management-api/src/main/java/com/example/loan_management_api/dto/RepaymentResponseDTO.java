package com.example.loan_management_api.dto;

import com.example.loan_management_api.model.enums.PaymentMethod;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepaymentResponseDTO {
    private Long id;
    private Long loanId;
    private Integer installmentNumber;
    private Double amountPaid;
    private LocalDateTime paymentDate;
    private PaymentMethod paymentMethod;
    private String transactionReference;
    private String notes;
    private Double remainingLoanBalance;
}
