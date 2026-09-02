package com.example.loan_management_api.dto;

import com.example.loan_management_api.model.enums.RepaymentStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmiScheduleDTO {
    private Long id;
    private Integer installmentNumber;
    private LocalDate dueDate;
    private Double emiAmount;
    private Double principalComponent;
    private Double interestComponent;
    private Double remainingPrincipal;
    private RepaymentStatus status;
    private LocalDateTime paidDate;
}
