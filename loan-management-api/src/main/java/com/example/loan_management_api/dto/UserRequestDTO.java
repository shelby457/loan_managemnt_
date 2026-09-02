package com.example.loan_management_api.dto;

import com.example.loan_management_api.model.enums.EmploymentStatus;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequestDTO {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone must be a valid 10-15 digit number")
    private String phone;

    @NotNull(message = "Monthly income is required")
    @Positive(message = "Monthly income must be greater than 0")
    private Double monthlyIncome;

    @Min(value = 300, message = "Credit score cannot be less than 300")
    @Max(value = 850, message = "Credit score cannot exceed 850")
    private Integer creditScore;

    private EmploymentStatus employmentStatus;

    private String address;
}
