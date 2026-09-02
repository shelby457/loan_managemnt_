package com.example.loan_management_api.repository;

import com.example.loan_management_api.model.EmiSchedule;
import com.example.loan_management_api.model.enums.RepaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmiScheduleRepository extends JpaRepository<EmiSchedule, Long> {
    List<EmiSchedule> findByLoanIdOrderByInstallmentNumberAsc(Long loanId);
    Optional<EmiSchedule> findFirstByLoanIdAndStatusOrderByInstallmentNumberAsc(Long loanId, RepaymentStatus status);
    List<EmiSchedule> findByStatusAndDueDateBefore(RepaymentStatus status, LocalDate date);
}
