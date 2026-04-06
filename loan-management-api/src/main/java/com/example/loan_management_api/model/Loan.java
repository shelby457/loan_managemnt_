package com.example.loan_management_api.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;
    private String status;

    @ManyToOne
    private User user;
}