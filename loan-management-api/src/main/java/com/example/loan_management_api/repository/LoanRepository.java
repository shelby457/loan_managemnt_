package com.example.loan_management_api.repository;

import com.example.loan_management_api.model.Loan;
import com.example.loan_management_api.model.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUserId(Long userId);
    List<Loan> findByStatus(LoanStatus status);
    List<Loan> findByUserIdAndStatus(Long userId, LoanStatus status);

    long countByStatus(LoanStatus status);

    @Query("SELECT COALESCE(SUM(l.principalAmount), 0.0) FROM Loan l WHERE l.status IN ('APPROVED', 'ACTIVE', 'FULLY_PAID')")
    Double sumTotalDisbursedAmount();

    @Query("SELECT COALESCE(SUM(l.remainingBalance), 0.0) FROM Loan l WHERE l.status = 'ACTIVE'")
    Double sumActiveRemainingBalance();

    @Query("SELECT COALESCE(SUM(l.totalInterest), 0.0) FROM Loan l WHERE l.status IN ('APPROVED', 'ACTIVE', 'FULLY_PAID')")
    Double sumTotalProjectedInterest();
}