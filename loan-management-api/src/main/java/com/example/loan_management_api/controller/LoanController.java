package com.example.loan_management_api.controller;

import com.example.loan_management_api.model.*;
import com.example.loan_management_api.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        return loanService.createUser(user);
    }

    @PostMapping("/loans/{userId}")
    public Loan applyLoan(@PathVariable Long userId, @RequestBody Loan loan) {
        return loanService.applyLoan(userId, loan);
    }

    @GetMapping("/loans/{userId}")
    public List<Loan> getLoans(@PathVariable Long userId) {
        return loanService.getLoans(userId);
    }

    @GetMapping("/test")
    public String test() {
        return "API Running";
    }
}