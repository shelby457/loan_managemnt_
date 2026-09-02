package com.example.loan_management_api.controller;

import com.example.loan_management_api.dto.ApiResponse;
import com.example.loan_management_api.dto.LoanCalculationRequestDTO;
import com.example.loan_management_api.dto.LoanCalculationResponseDTO;
import com.example.loan_management_api.service.EmiCalculatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calculator")
@Tag(name = "Financial Calculator", description = "Endpoints for calculating EMIs, interest breakdowns, and amortization schedules without persisting")
@CrossOrigin(origins = "*")
public class CalculatorController {

    @Autowired
    private EmiCalculatorService calculatorService;

    @PostMapping("/calculate")
    @Operation(summary = "Calculate monthly EMI and complete amortization schedule")
    public ResponseEntity<ApiResponse<LoanCalculationResponseDTO>> calculateLoan(
            @Valid @RequestBody LoanCalculationRequestDTO request) {
        LoanCalculationResponseDTO response = calculatorService.calculateLoanDetails(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
