# LendFlow - Enterprise Loan Management & Expense Splitwise Platform

An enterprise-grade Spring Boot & Java backend and modern financial dashboard for loan origination, credit underwriting, reducing-balance EMI calculation, amortization scheduling, repayment ledger tracking, monthly expenditure tracking, and Splitwise-style group bill splitting with simplified pairwise debt settlements.

---

## Key Features

- **Automated Credit Underwriting**: Real-time borrower eligibility risk scoring based on Credit Score (300-850), Debt-to-Income (DTI) ratio, and income capacity multipliers.
- **Financial EMI Engine**: Accurate reducing-balance amortization calculator supporting multiple loan products (Personal, Home, Auto, Education, Business) with full month-by-month principal vs. interest breakdown.
- **Full Loan Lifecycle**: Application $\rightarrow$ Risk Review $\rightarrow$ Approval / Underwriting Override $\rightarrow$ Active Disbursement $\rightarrow$ Installment Repayments $\rightarrow$ Auto-Closure.
- **Repayment Ledger**: Multi-channel payment recording (UPI, ACH, Card, Bank Wire) with automatic remaining balance deduction, installment reconciliation, and unique transaction references.
- **Monthly Expense Tracker & Splitwise Engine**: 
  - Log categorized monthly personal & group expenditures (Housing, Utilities, Food & Dining, Office, Travel, Healthcare, etc.).
  - Equal, Exact, or Percentage group bill splitting among registered members.
  - Splitwise pairwise debt graph simplification ($O(N^2)$ matrix reduction) computing net balances ("who owes who how much") and 1-click debt settlement with transaction references.
- **Modern Glassmorphic Web Dashboard**: Real-time portfolio metrics, Chart.js visual analytics, borrower directory, interactive slider simulator, monthly expenses board, and payment processor UI served directly on `http://localhost:8080/`.
- **OpenAPI 3 / Swagger Documentation**: Interactive API testing console at `http://localhost:8080/swagger-ui.html`.
- **Embedded H2 Database Console**: Live database browser at `http://localhost:8080/h2-console`.

---

## Tech Stack

- **Backend**: Java 17, Spring Boot 3.5.x, Spring Data JPA, Spring Web, Spring Validation, Lombok
- **Documentation**: SpringDoc OpenAPI 3.0 / Swagger UI
- **Database**: H2 in-memory embedded SQL database
- **Frontend**: Glassmorphic SPA (HTML5, Vanilla CSS3, JavaScript ES6+, Chart.js, FontAwesome)

---

## API Endpoints

### 1. Borrower Management (`/api/users`)
- `POST /api/users` - Register a new borrower
- `GET /api/users` - List all registered borrowers
- `GET /api/users/{id}` - Get borrower profile & loan history
- `PUT /api/users/{id}` - Update borrower profile
- `DELETE /api/users/{id}` - Remove borrower

### 2. Loan Origination & Underwriting (`/api/loans`)
- `POST /api/loans/apply` - Submit new loan application
- `POST /api/loans/{id}/approve` - Approve & activate loan (generates EMI schedule)
- `POST /api/loans/{id}/reject` - Reject loan with reason
- `GET /api/loans` - Query all loans (supports `?status=PENDING|ACTIVE|FULLY_PAID|REJECTED`)
- `GET /api/loans/{id}` - Get loan details with full EMI schedule & payment transactions
- `GET /api/loans/user/{userId}` - Get all loans for a borrower
- `GET /api/loans/eligibility` - Run real-time underwriting risk assessment

### 3. Repayment & Ledger (`/api/repayments`)
- `POST /api/repayments/pay` - Post installment repayment
- `GET /api/repayments/loan/{loanId}` - Get transaction ledger for a loan
- `GET /api/repayments/schedule/{loanId}` - Get EMI amortization schedule

### 4. Expense Tracker & Splitwise (`/api/expenses`)
- `POST /api/expenses` - Create expense (personal or shared group split)
- `GET /api/expenses` - Query expenses with optional `?year=`, `?month=`, `?category=`, `?payerId=`
- `GET /api/expenses/{id}` - Get single expense with participant shares
- `DELETE /api/expenses/{id}` - Delete expense and its split records
- `GET /api/expenses/summary` - Get monthly spending analytics (category and payer breakdowns)
- `GET /api/expenses/splitwise/board` - Compute member net balances and simplified peer debts
- `POST /api/expenses/splitwise/settle` - Record peer-to-peer debt settlement
- `GET /api/expenses/splitwise/settlements` - Retrieve settlement audit history

### 5. Financial Calculator & Analytics (`/api/calculator`, `/api/dashboard`)
- `POST /api/calculator/calculate` - Calculate EMI & schedule without saving
- `GET /api/dashboard/stats` - Get live portfolio KPI metrics & chart data

---

## How to Run

### 1. Build and Run with Maven Wrapper
```powershell
cd loan_management/loan-management-api
.\mvnw.cmd spring-boot:run
```

### 2. Access the Application
- **Web Dashboard**: [http://localhost:8080](http://localhost:8080)
- **Interactive Swagger API Docs**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **H2 Database Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)  
  *(JDBC URL: `jdbc:h2:mem:loandb`, Username: `sa`, Password: empty)*

