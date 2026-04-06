package com.example.loan_management_api.service;

import com.example.loan_management_api.model.*;
import com.example.loan_management_api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoanService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoanRepository loanRepository;

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public Loan applyLoan(Long userId, Loan loan) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        loan.setUser(user);
        loan.setStatus("PENDING");

        return loanRepository.save(loan);
    }

    public List<Loan> getLoans(Long userId) {
        return loanRepository.findByUserId(userId);
    }
}