package com.example.loan_management_api.service;

import com.example.loan_management_api.dto.CreditEvaluationDTO;
import com.example.loan_management_api.model.User;
import com.example.loan_management_api.model.enums.EmploymentStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CreditUnderwritingService {

    public CreditEvaluationDTO evaluateEligibility(User user, double requestedAmount, double monthlyEmi) {
        int score = user.getCreditScore() != null ? user.getCreditScore() : 650;
        double income = user.getMonthlyIncome() != null ? user.getMonthlyIncome() : 0.0;
        EmploymentStatus empStatus = user.getEmploymentStatus() != null ? user.getEmploymentStatus() : EmploymentStatus.EMPLOYED;

        List<String> notes = new ArrayList<>();
        String creditRating;
        String riskLevel;
        boolean eligible = true;

        if (score >= 750) {
            creditRating = "EXCELLENT";
            riskLevel = "LOW";
            notes.add("Credit score is excellent (>750). Prime rate eligible.");
        } else if (score >= 670) {
            creditRating = "GOOD";
            riskLevel = "LOW";
            notes.add("Credit score is good (670-749). Standard underwriting approval.");
        } else if (score >= 580) {
            creditRating = "FAIR";
            riskLevel = "MEDIUM";
            notes.add("Credit score is fair (580-669). Moderate risk profile.");
        } else {
            creditRating = "POOR";
            riskLevel = "HIGH";
            notes.add("Subprime credit score (<580). Requires collateral or co-signer.");
            eligible = false;
        }

        // Maximum loan capacity based on income multiplier
        double incomeMultiplier = switch (empStatus) {
            case EMPLOYED -> 30.0;
            case BUSINESS_OWNER, SELF_EMPLOYED -> 25.0;
            case STUDENT -> 5.0;
            case UNEMPLOYED -> 0.0;
        };

        double estimatedMaxLoan = EmiCalculatorService.round(income * incomeMultiplier);

        // Debt to income check (EMI should not exceed 50% of monthly income)
        double dti = (income > 0) ? EmiCalculatorService.round((monthlyEmi / income) * 100.0) : 100.0;

        if (dti > 50.0) {
            notes.add("High Debt-to-Income ratio (" + dti + "%). Recommended EMI must be <= 50% of income.");
            if (dti > 65.0) {
                eligible = false;
            }
        } else {
            notes.add("Healthy Debt-to-Income ratio (" + dti + "%).");
        }

        if (requestedAmount > estimatedMaxLoan) {
            notes.add("Requested amount ($" + requestedAmount + ") exceeds maximum recommended limit ($" + estimatedMaxLoan + ").");
            eligible = false;
        }

        if (empStatus == EmploymentStatus.UNEMPLOYED) {
            notes.add("Applicant is currently unemployed.");
            eligible = false;
        }

        return CreditEvaluationDTO.builder()
                .eligible(eligible)
                .riskLevel(riskLevel)
                .creditScore(score)
                .creditRating(creditRating)
                .estimatedMaxLoanAmount(estimatedMaxLoan)
                .debtToIncomeRatio(dti)
                .underwritingNotes(notes)
                .build();
    }

    public String getRatingFromScore(int score) {
        if (score >= 750) return "EXCELLENT";
        if (score >= 670) return "GOOD";
        if (score >= 580) return "FAIR";
        return "POOR";
    }
}
