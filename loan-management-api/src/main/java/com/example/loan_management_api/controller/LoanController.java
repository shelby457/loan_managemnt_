package com.example.loan_management_api.controller;

import com.example.loan_management_api.dto.*;
import com.example.loan_management_api.model.enums.LoanStatus;
import com.example.loan_management_api.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@Tag(name = "Loan Management", description = "Endpoints for loan applications, underwriting approvals, and queries")
@CrossOrigin(origins = "*")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @PostMapping("/apply")
    @Operation(summary = "Submit a new loan application")
    public ResponseEntity<ApiResponse<LoanResponseDTO>> applyLoan(@Valid @RequestBody LoanApplicationDTO request) {
        LoanResponseDTO loan = loanService.applyLoan(request);
        return new ResponseEntity<>(ApiResponse.success("Loan application submitted successfully", loan), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve a pending loan application and generate EMI schedule")
    public ResponseEntity<ApiResponse<LoanResponseDTO>> approveLoan(
            @PathVariable Long id,
            @RequestBody(required = false) LoanApprovalDTO approvalDTO) {
        LoanResponseDTO loan = loanService.approveLoan(id, approvalDTO);
        return ResponseEntity.ok(ApiResponse.success("Loan approved and activated successfully", loan));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject a pending loan application")
    public ResponseEntity<ApiResponse<LoanResponseDTO>> rejectLoan(
            @PathVariable Long id,
            @Valid @RequestBody LoanRejectionDTO rejectionDTO) {
        LoanResponseDTO loan = loanService.rejectLoan(id, rejectionDTO);
        return ResponseEntity.ok(ApiResponse.success("Loan rejected", loan));
    }

    @GetMapping
    @Operation(summary = "Get all loans with optional status filter")
    public ResponseEntity<ApiResponse<List<LoanResponseDTO>>> getAllLoans(
            @RequestParam(required = false) LoanStatus status) {
        List<LoanResponseDTO> loans = loanService.getAllLoans(status);
        return ResponseEntity.ok(ApiResponse.success(loans));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get loan details with EMI schedule and repayment ledger by ID")
    public ResponseEntity<ApiResponse<LoanResponseDTO>> getLoanById(@PathVariable Long id) {
        LoanResponseDTO loan = loanService.getLoanById(id);
        return ResponseEntity.ok(ApiResponse.success(loan));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all loans for a specific borrower")
    public ResponseEntity<ApiResponse<List<LoanResponseDTO>>> getLoansByUserId(
            @PathVariable Long userId,
            @RequestParam(required = false) LoanStatus status) {
        List<LoanResponseDTO> loans = loanService.getLoansByUserId(userId, status);
        return ResponseEntity.ok(ApiResponse.success(loans));
    }

    @GetMapping("/eligibility")
    @Operation(summary = "Evaluate loan eligibility and underwriting risk")
    public ResponseEntity<ApiResponse<CreditEvaluationDTO>> evaluateEligibility(
            @RequestParam Long userId,
            @RequestParam Double principalAmount,
            @RequestParam Integer termMonths) {
        CreditEvaluationDTO eval = loanService.evaluateLoanEligibility(userId, principalAmount, termMonths);
        return ResponseEntity.ok(ApiResponse.success(eval));
    }
}