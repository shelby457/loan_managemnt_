package com.example.loan_management_api.controller;

import com.example.loan_management_api.dto.ApiResponse;
import com.example.loan_management_api.dto.EmiScheduleDTO;
import com.example.loan_management_api.dto.RepaymentRequestDTO;
import com.example.loan_management_api.dto.RepaymentResponseDTO;
import com.example.loan_management_api.service.RepaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repayments")
@Tag(name = "Repayment & Ledger", description = "Endpoints for processing EMI installment payments and viewing amortization schedules")
@CrossOrigin(origins = "*")
public class RepaymentController {

    @Autowired
    private RepaymentService repaymentService;

    @PostMapping("/pay")
    @Operation(summary = "Make an EMI installment repayment")
    public ResponseEntity<ApiResponse<RepaymentResponseDTO>> payInstallment(
            @Valid @RequestBody RepaymentRequestDTO request) {
        RepaymentResponseDTO payment = repaymentService.processRepayment(request);
        return new ResponseEntity<>(ApiResponse.success("Payment recorded successfully", payment), HttpStatus.CREATED);
    }

    @GetMapping("/loan/{loanId}")
    @Operation(summary = "Get repayment transaction history for a loan")
    public ResponseEntity<ApiResponse<List<RepaymentResponseDTO>>> getLoanRepayments(@PathVariable Long loanId) {
        List<RepaymentResponseDTO> list = repaymentService.getRepaymentsByLoanId(loanId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/schedule/{loanId}")
    @Operation(summary = "Get full EMI amortization schedule for a loan")
    public ResponseEntity<ApiResponse<List<EmiScheduleDTO>>> getLoanSchedule(@PathVariable Long loanId) {
        List<EmiScheduleDTO> list = repaymentService.getEmiSchedulesByLoanId(loanId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
