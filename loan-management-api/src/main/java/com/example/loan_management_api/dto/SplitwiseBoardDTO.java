package com.example.loan_management_api.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SplitwiseBoardDTO {
    private Double totalGroupSpending;
    private Double totalUnsettledDebt;
    private List<UserBalanceDTO> userBalances;
    private List<PeerDebtDTO> simplifiedDebts;
}
