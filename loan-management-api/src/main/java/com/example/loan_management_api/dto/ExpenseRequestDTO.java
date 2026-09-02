package com.example.loan_management_api.dto;

import com.example.loan_management_api.model.enums.ExpenseCategory;
import com.example.loan_management_api.model.enums.SplitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseRequestDTO {

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Amount is required")
    @Positive(message = "Expense amount must be positive")
    private Double amount;

    private ExpenseCategory category;

    private LocalDate expenseDate;

    @NotNull(message = "Payer user ID is required")
    private Long payerId;

    private SplitType splitType;

    private String notes;

    private Boolean isSplit;

    private List<ExpenseSplitRequestDTO> splits; // List of participants to split with
}
