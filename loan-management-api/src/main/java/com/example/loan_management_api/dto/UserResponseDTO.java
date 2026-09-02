package com.example.loan_management_api.dto;

import com.example.loan_management_api.model.enums.EmploymentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Double monthlyIncome;
    private Integer creditScore;
    private String creditRating; // EXCELLENT, GOOD, FAIR, POOR
    private EmploymentStatus employmentStatus;
    private String address;
    private int totalLoans;
    private int activeLoans;
    private Double totalBorrowed;
    private LocalDateTime createdAt;
}
