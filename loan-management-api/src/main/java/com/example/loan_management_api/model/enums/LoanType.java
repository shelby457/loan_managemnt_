package com.example.loan_management_api.model.enums;

public enum LoanType {
    PERSONAL(12.5, 600000.0, 60),
    HOME(8.5, 10000000.0, 360),
    AUTO(9.0, 3000000.0, 84),
    EDUCATION(10.0, 4000000.0, 120),
    BUSINESS(14.0, 20000000.0, 120);

    private final double defaultInterestRate;
    private final double maxLoanAmount;
    private final int maxTermMonths;

    LoanType(double defaultInterestRate, double maxLoanAmount, int maxTermMonths) {
        this.defaultInterestRate = defaultInterestRate;
        this.maxLoanAmount = maxLoanAmount;
        this.maxTermMonths = maxTermMonths;
    }

    public double getDefaultInterestRate() {
        return defaultInterestRate;
    }

    public double getMaxLoanAmount() {
        return maxLoanAmount;
    }

    public int getMaxTermMonths() {
        return maxTermMonths;
    }
}
