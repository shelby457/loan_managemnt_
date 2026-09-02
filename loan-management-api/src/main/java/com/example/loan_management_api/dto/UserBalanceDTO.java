package com.example.loan_management_api.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBalanceDTO {
    private Long userId;
    private String userName;
    private String userEmail;
    private Double totalPaidForGroup;
    private Double totalOwedToGroup;
    private Double netBalance; // Positive = gets back, Negative = owes
    private String status; // "OWED", "OWES", "SETTLED"
}
