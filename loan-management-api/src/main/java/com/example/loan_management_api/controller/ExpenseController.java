package com.example.loan_management_api.controller;

import com.example.loan_management_api.dto.*;
import com.example.loan_management_api.model.enums.ExpenseCategory;
import com.example.loan_management_api.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@Tag(name = "Expenses & Splitwise Tracker", description = "Endpoints for monthly personal & group expense tracking, Splitwise-style peer bill splitting, and debt settlements")
@CrossOrigin(origins = "*")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @PostMapping
    @Operation(summary = "Add an expense (with optional Splitwise-style group split)")
    public ResponseEntity<ApiResponse<ExpenseResponseDTO>> createExpense(@Valid @RequestBody ExpenseRequestDTO request) {
        ExpenseResponseDTO expense = expenseService.createExpense(request);
        return new ResponseEntity<>(ApiResponse.success("Expense logged successfully", expense), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all expenses with optional month, year, category, and payer filters")
    public ResponseEntity<ApiResponse<List<ExpenseResponseDTO>>> getAllExpenses(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) ExpenseCategory category,
            @RequestParam(required = false) Long payerId) {
        List<ExpenseResponseDTO> list = expenseService.getAllExpenses(year, month, category, payerId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get expense details by ID")
    public ResponseEntity<ApiResponse<ExpenseResponseDTO>> getExpenseById(@PathVariable Long id) {
        ExpenseResponseDTO expense = expenseService.getExpenseById(id);
        return ResponseEntity.ok(ApiResponse.success(expense));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an expense")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.ok(ApiResponse.success("Expense deleted successfully", null));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get monthly expense summary and category breakdown")
    public ResponseEntity<ApiResponse<MonthlyExpenseSummaryDTO>> getMonthlySummary(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        int targetYear = (year != null) ? year : LocalDate.now().getYear();
        int targetMonth = (month != null) ? month : LocalDate.now().getMonthValue();

        MonthlyExpenseSummaryDTO summary = expenseService.getMonthlySummary(targetYear, targetMonth);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/splitwise/board")
    @Operation(summary = "Get Splitwise balance board and simplified peer debt matrix")
    public ResponseEntity<ApiResponse<SplitwiseBoardDTO>> getSplitwiseBoard() {
        SplitwiseBoardDTO board = expenseService.getSplitwiseBoard();
        return ResponseEntity.ok(ApiResponse.success(board));
    }

    @PostMapping("/splitwise/settle")
    @Operation(summary = "Settle up debt between two users (Record a peer payment)")
    public ResponseEntity<ApiResponse<SettlementResponseDTO>> settleDebt(@Valid @RequestBody SettlementRequestDTO request) {
        SettlementResponseDTO settlement = expenseService.recordSettlement(request);
        return new ResponseEntity<>(ApiResponse.success("Debt settled successfully", settlement), HttpStatus.CREATED);
    }

    @GetMapping("/splitwise/settlements")
    @Operation(summary = "Get all peer settlement records")
    public ResponseEntity<ApiResponse<List<SettlementResponseDTO>>> getSettlements() {
        List<SettlementResponseDTO> list = expenseService.getAllSettlements();
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
