package com.example.loan_management_api.repository;

import com.example.loan_management_api.model.ExpenseSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, Long> {
    List<ExpenseSplit> findByUserId(Long userId);
    List<ExpenseSplit> findByUserIdAndSettled(Long userId, Boolean settled);
    List<ExpenseSplit> findByExpenseId(Long expenseId);
}
