package com.example.loan_management_api.repository;

import com.example.loan_management_api.model.Repayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepaymentRepository extends JpaRepository<Repayment, Long> {
    List<Repayment> findByLoanIdOrderByPaymentDateDesc(Long loanId);

    @Query("SELECT COALESCE(SUM(r.amountPaid), 0.0) FROM Repayment r")
    Double sumTotalRepaymentsCollected();

    @Query("SELECT COALESCE(SUM(r.amountPaid), 0.0) FROM Repayment r WHERE r.loan.id = :loanId")
    Double sumTotalPaidForLoan(Long loanId);
}
