package com.example.loan_management_api.repository;

import com.example.loan_management_api.model.Expense;
import com.example.loan_management_api.model.enums.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByPayerIdOrderByExpenseDateDesc(Long payerId);

    List<Expense> findByExpenseDateBetweenOrderByExpenseDateDesc(LocalDate startDate, LocalDate endDate);

    List<Expense> findByCategoryOrderByExpenseDateDesc(ExpenseCategory category);

    @Query("SELECT COALESCE(SUM(e.amount), 0.0) FROM Expense e WHERE e.expenseDate BETWEEN :startDate AND :endDate")
    Double sumTotalExpenseInPeriod(LocalDate startDate, LocalDate endDate);

    @Query("SELECT COALESCE(SUM(e.amount), 0.0) FROM Expense e WHERE e.payer.id = :payerId AND e.expenseDate BETWEEN :startDate AND :endDate")
    Double sumTotalExpenseByPayerInPeriod(Long payerId, LocalDate startDate, LocalDate endDate);
}
