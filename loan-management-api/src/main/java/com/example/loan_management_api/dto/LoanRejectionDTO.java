package com.example.loan_management_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRejectionDTO {
    @NotBlank(message = "Rejection reason is required")
    private String reason;
}
