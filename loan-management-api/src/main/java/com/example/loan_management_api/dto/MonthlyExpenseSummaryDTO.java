package com.example.loan_management_api.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyExpenseSummaryDTO {
    private int year;
    private int month;
    private String monthName;
    private Double totalSpent;
    private int totalExpenseCount;
    private Double averageExpense;
    private Map<String, Double> expensesByCategory;
    private Map<String, Double> expensesByPayer;
}
