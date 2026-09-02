package com.example.loan_management_api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseSplitRequestDTO {

    @NotNull(message = "User ID is required for split participant")
    private Long userId;

    private Double owedAmount; // Required for EXACT_AMOUNT or computed for EQUAL

    private Double percentage; // Optional for PERCENTAGE split
}
