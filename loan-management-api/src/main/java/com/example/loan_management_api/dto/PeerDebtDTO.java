package com.example.loan_management_api.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeerDebtDTO {
    private Long debtorId; // Who owes
    private String debtorName;
    private Long creditorId; // Who is owed
    private String creditorName;
    private Double amount;
    private String summaryText; // e.g. "Carlos owes Priya $45.00"
}
