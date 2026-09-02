package com.example.loan_management_api.model;

import com.example.loan_management_api.model.enums.LoanStatus;
import com.example.loan_management_api.model.enums.LoanType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LoanType loanType = LoanType.PERSONAL;

    @Column(nullable = false)
    private Double principalAmount;

    @Column(nullable = false)
    private Double interestRate; // Annual percentage e.g., 10.5

    @Column(nullable = false)
    private Integer termMonths; // Duration in months

    private Double monthlyEmi;

    private Double totalInterest;

    private Double totalRepayable;

    private Double remainingBalance;

    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LoanStatus status = LoanStatus.PENDING;

    private String rejectionReason;

    private LocalDateTime appliedDate;

    private LocalDateTime approvedDate;

    private LocalDateTime closedDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"loans", "createdAt", "updatedAt"})
    private User user;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("installmentNumber ASC")
    @Builder.Default
    private List<EmiSchedule> emiSchedules = new ArrayList<>();

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("paymentDate DESC")
    @Builder.Default
    private List<Repayment> repayments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.appliedDate == null) {
            this.appliedDate = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = LoanStatus.PENDING;
        }
    }
}