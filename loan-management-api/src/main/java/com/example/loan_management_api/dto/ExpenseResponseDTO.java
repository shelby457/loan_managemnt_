package com.example.loan_management_api.dto;

import com.example.loan_management_api.model.enums.ExpenseCategory;
import com.example.loan_management_api.model.enums.SplitType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseResponseDTO {
    private Long id;
    private String description;
    private Double amount;
    private ExpenseCategory category;
    private LocalDate expenseDate;
    private Long payerId;
    private String payerName;
    private String payerEmail;
    private SplitType splitType;
    private String notes;
    private Boolean isSplit;
    private List<ExpenseSplitResponseDTO> splits;
    private LocalDateTime createdAt;
}
