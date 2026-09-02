package com.example.loan_management_api.service;

import com.example.loan_management_api.dto.EmiScheduleDTO;
import com.example.loan_management_api.dto.RepaymentRequestDTO;
import com.example.loan_management_api.dto.RepaymentResponseDTO;
import com.example.loan_management_api.exception.InvalidLoanOperationException;
import com.example.loan_management_api.exception.ResourceNotFoundException;
import com.example.loan_management_api.model.EmiSchedule;
import com.example.loan_management_api.model.Loan;
import com.example.loan_management_api.model.Repayment;
import com.example.loan_management_api.model.enums.LoanStatus;
import com.example.loan_management_api.model.enums.PaymentMethod;
import com.example.loan_management_api.model.enums.RepaymentStatus;
import com.example.loan_management_api.repository.EmiScheduleRepository;
import com.example.loan_management_api.repository.LoanRepository;
import com.example.loan_management_api.repository.RepaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RepaymentService {

    @Autowired
    private RepaymentRepository repaymentRepository;

    @Autowired
    private EmiScheduleRepository emiScheduleRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Transactional
    public RepaymentResponseDTO processRepayment(RepaymentRequestDTO request) {
        Loan loan = loanRepository.findById(request.getLoanId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + request.getLoanId()));

        if (loan.getStatus() != LoanStatus.ACTIVE && loan.getStatus() != LoanStatus.APPROVED) {
            throw new InvalidLoanOperationException("Cannot process payment for loan with status: " + loan.getStatus());
        }

        if (loan.getRemainingBalance() <= 0) {
            throw new InvalidLoanOperationException("Loan is already fully repaid.");
        }

        // Find next unpaid installment
        Optional<EmiSchedule> nextScheduleOpt = emiScheduleRepository
                .findFirstByLoanIdAndStatusOrderByInstallmentNumberAsc(loan.getId(), RepaymentStatus.PENDING);

        Integer installmentNumber = null;
        if (nextScheduleOpt.isPresent()) {
            EmiSchedule schedule = nextScheduleOpt.get();
            installmentNumber = schedule.getInstallmentNumber();
            schedule.setStatus(RepaymentStatus.PAID);
            schedule.setPaidDate(LocalDateTime.now());
            emiScheduleRepository.save(schedule);
        }

        // Deduct from remaining balance
        double newRemaining = Math.max(0.0, EmiCalculatorService.round(loan.getRemainingBalance() - request.getAmount()));
        loan.setRemainingBalance(newRemaining);

        if (newRemaining <= 0.0) {
            loan.setStatus(LoanStatus.FULLY_PAID);
            loan.setClosedDate(LocalDateTime.now());
        }

        loanRepository.save(loan);

        // Record Repayment
        String txRef = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Repayment repayment = Repayment.builder()
                .loan(loan)
                .installmentNumber(installmentNumber)
                .amountPaid(request.getAmount())
                .paymentDate(LocalDateTime.now())
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.UPI)
                .transactionReference(txRef)
                .notes(request.getNotes())
                .build();

        Repayment saved = repaymentRepository.save(repayment);

        return RepaymentResponseDTO.builder()
                .id(saved.getId())
                .loanId(loan.getId())
                .installmentNumber(installmentNumber)
                .amountPaid(saved.getAmountPaid())
                .paymentDate(saved.getPaymentDate())
                .paymentMethod(saved.getPaymentMethod())
                .transactionReference(saved.getTransactionReference())
                .notes(saved.getNotes())
                .remainingLoanBalance(newRemaining)
                .build();
    }

    public List<RepaymentResponseDTO> getRepaymentsByLoanId(Long loanId) {
        return repaymentRepository.findByLoanIdOrderByPaymentDateDesc(loanId).stream()
                .map(r -> RepaymentResponseDTO.builder()
                        .id(r.getId())
                        .loanId(loanId)
                        .installmentNumber(r.getInstallmentNumber())
                        .amountPaid(r.getAmountPaid())
                        .paymentDate(r.getPaymentDate())
                        .paymentMethod(r.getPaymentMethod())
                        .transactionReference(r.getTransactionReference())
                        .notes(r.getNotes())
                        .remainingLoanBalance(r.getLoan().getRemainingBalance())
                        .build())
                .collect(Collectors.toList());
    }

    public List<EmiScheduleDTO> getEmiSchedulesByLoanId(Long loanId) {
        return emiScheduleRepository.findByLoanIdOrderByInstallmentNumberAsc(loanId).stream()
                .map(s -> EmiScheduleDTO.builder()
                        .id(s.getId())
                        .installmentNumber(s.getInstallmentNumber())
                        .dueDate(s.getDueDate())
                        .emiAmount(s.getEmiAmount())
                        .principalComponent(s.getPrincipalComponent())
                        .interestComponent(s.getInterestComponent())
                        .remainingPrincipal(s.getRemainingPrincipal())
                        .status(s.getStatus())
                        .paidDate(s.getPaidDate())
                        .build())
                .collect(Collectors.toList());
    }
}
