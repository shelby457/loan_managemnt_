package com.example.loan_management_api.service;

import com.example.loan_management_api.dto.EmiScheduleDTO;
import com.example.loan_management_api.dto.LoanCalculationRequestDTO;
import com.example.loan_management_api.dto.LoanCalculationResponseDTO;
import com.example.loan_management_api.model.EmiSchedule;
import com.example.loan_management_api.model.Loan;
import com.example.loan_management_api.model.enums.RepaymentStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmiCalculatorService {

    /**
     * Calculates the monthly EMI using standard reducing balance formula:
     * EMI = [P x r x (1+r)^n] / [(1+r)^n - 1]
     * where:
     * P = Principal loan amount
     * r = Monthly interest rate (Annual rate / 12 / 100)
     * n = Number of monthly installments
     */
    public double calculateMonthlyEmi(double principal, double annualInterestRate, int termMonths) {
        if (principal <= 0 || termMonths <= 0) return 0.0;
        if (annualInterestRate <= 0) {
            return round(principal / termMonths);
        }

        double monthlyRate = annualInterestRate / (12.0 * 100.0);
        double emi = (principal * monthlyRate * Math.pow(1.0 + monthlyRate, termMonths))
                / (Math.pow(1.0 + monthlyRate, termMonths) - 1.0);

        return round(emi);
    }

    public LoanCalculationResponseDTO calculateLoanDetails(LoanCalculationRequestDTO request) {
        double principal = request.getPrincipalAmount();
        double annualRate = request.getAnnualInterestRate();
        int term = request.getTermMonths();

        double emi = calculateMonthlyEmi(principal, annualRate, term);
        double totalRepayable = round(emi * term);
        double totalInterest = round(totalRepayable - principal);
        double interestPercentage = round((totalInterest / totalRepayable) * 100.0);

        List<EmiScheduleDTO> schedule = generateAmortizationSchedule(principal, annualRate, term, emi);

        return LoanCalculationResponseDTO.builder()
                .principalAmount(principal)
                .annualInterestRate(annualRate)
                .termMonths(term)
                .monthlyEmi(emi)
                .totalInterest(totalInterest)
                .totalRepayable(totalRepayable)
                .interestPercentageOfTotal(interestPercentage)
                .amortizationSchedule(schedule)
                .build();
    }

    public List<EmiScheduleDTO> generateAmortizationSchedule(double principal, double annualRate, int termMonths, double emi) {
        List<EmiScheduleDTO> schedules = new ArrayList<>();
        double monthlyRate = annualRate / (12.0 * 100.0);
        double remainingBalance = principal;
        LocalDate startDate = LocalDate.now().plusMonths(1);

        for (int i = 1; i <= termMonths; i++) {
            double interestPart = round(remainingBalance * monthlyRate);
            double principalPart = round(emi - interestPart);

            // Handle last installment discrepancy
            if (i == termMonths || principalPart > remainingBalance) {
                principalPart = remainingBalance;
                interestPart = round(emi - principalPart);
                remainingBalance = 0.0;
            } else {
                remainingBalance = round(remainingBalance - principalPart);
            }

            schedules.add(EmiScheduleDTO.builder()
                    .installmentNumber(i)
                    .dueDate(startDate.plusMonths(i - 1))
                    .emiAmount(emi)
                    .principalComponent(principalPart)
                    .interestComponent(Math.max(0.0, interestPart))
                    .remainingPrincipal(Math.max(0.0, remainingBalance))
                    .status(RepaymentStatus.PENDING)
                    .build());
        }

        return schedules;
    }

    public List<EmiSchedule> generateEntitySchedule(Loan loan, LocalDate firstDueDate) {
        List<EmiSchedule> list = new ArrayList<>();
        double monthlyRate = loan.getInterestRate() / (12.0 * 100.0);
        double remainingBalance = loan.getPrincipalAmount();
        double emi = loan.getMonthlyEmi();
        LocalDate baseDate = (firstDueDate != null) ? firstDueDate : LocalDate.now().plusMonths(1);

        for (int i = 1; i <= loan.getTermMonths(); i++) {
            double interestPart = round(remainingBalance * monthlyRate);
            double principalPart = round(emi - interestPart);

            if (i == loan.getTermMonths() || principalPart > remainingBalance) {
                principalPart = remainingBalance;
                interestPart = round(emi - principalPart);
                remainingBalance = 0.0;
            } else {
                remainingBalance = round(remainingBalance - principalPart);
            }

            EmiSchedule schedule = EmiSchedule.builder()
                    .loan(loan)
                    .installmentNumber(i)
                    .dueDate(baseDate.plusMonths(i - 1))
                    .emiAmount(emi)
                    .principalComponent(principalPart)
                    .interestComponent(Math.max(0.0, interestPart))
                    .remainingPrincipal(Math.max(0.0, remainingBalance))
                    .status(RepaymentStatus.PENDING)
                    .build();

            list.add(schedule);
        }

        return list;
    }

    public static double round(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
