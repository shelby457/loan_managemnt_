package com.example.loan_management_api.repository;

import com.example.loan_management_api.model.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findByPayerIdOrPayeeIdOrderBySettlementDateDesc(Long payerId, Long payeeId);
    List<Settlement> findAllByOrderBySettlementDateDesc();
}
