package com.example.loan_management_api.model;

import com.example.loan_management_api.model.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "settlements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payer_id", nullable = false)
    private User payer; // Who pays to settle

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payee_id", nullable = false)
    private User payee; // Who receives payment

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDateTime settlementDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.UPI;

    private String transactionReference;

    private String notes;

    @PrePersist
    protected void onCreate() {
        if (this.settlementDate == null) {
            this.settlementDate = LocalDateTime.now();
        }
    }
}
