package com.example.loan_management_api.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseSplitResponseDTO {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Double owedAmount;
    private Double percentage;
    private Boolean settled;
    private LocalDateTime settledDate;
}
