// API Base URL
const API_BASE = '/api';

// Application State
let currentLoans = [];
let currentBorrowers = [];
let currentExpenses = [];
let selectedLoanForRepayment = null;
let loanTypeChartInstance = null;
let loanStatusChartInstance = null;
let calcChartInstance = null;
let expenseCategoryChartInstance = null;
let expensePayerChartInstance = null;

// Initialize on DOM Load
document.addEventListener('DOMContentLoaded', () => {
    initNavigation();
    initModals();
    loadDashboardData();
    loadBorrowers();
    loadLoans();
    runCalculator();
    loadExpenseData();
});

// ----------------------------------------------------
// Navigation & Views
// ----------------------------------------------------
function initNavigation() {
    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach(item => {
        item.addEventListener('click', () => {
            const view = item.getAttribute('data-view');
            switchView(view);
        });
    });
}

function switchView(viewName) {
    // Update nav buttons
    document.querySelectorAll('.nav-item').forEach(el => {
        if (el.getAttribute('data-view') === viewName) {
            el.classList.add('active');
        } else {
            el.classList.remove('active');
        }
    });

    // Update view panels
    document.querySelectorAll('.view-panel').forEach(panel => {
        panel.classList.remove('active');
    });

    const targetPanel = document.getElementById(`view-${viewName}`);
    if (targetPanel) {
        targetPanel.classList.add('active');
    }

    // Update Header Titles
    const titleEl = document.getElementById('page-title');
    const descEl = document.getElementById('page-desc');

    switch (viewName) {
        case 'dashboard':
            titleEl.innerText = 'Executive Dashboard';
            descEl.innerText = 'Real-time loan portfolio monitoring and risk analytics';
            loadDashboardData();
            break;
        case 'loans':
            titleEl.innerText = 'Loan Management & Underwriting';
            descEl.innerText = 'Review pending applications, active accounts, and approval workflows';
            loadLoans();
            break;
        case 'borrowers':
            titleEl.innerText = 'Borrower Directory';
            descEl.innerText = 'Manage customer credit profiles, incomes, and historical debts';
            loadBorrowers();
            break;
        case 'repayments':
            titleEl.innerText = 'Repayments & Amortization Schedules';
            descEl.innerText = 'Track monthly installment ledgers and post live payments';
            loadRepaymentLoans();
            break;
        case 'calculator':
            titleEl.innerText = 'Financial EMI Simulator';
            descEl.innerText = 'Interactive reducing-balance amortization calculation engine';
            runCalculator();
            break;
        case 'expenses':
            titleEl.innerText = 'Monthly Expenses & Splitwise Engine';
            descEl.innerText = 'Track personal expenditures, group shared bills, and simplified peer settlements';
            loadExpenseData();
            break;
    }
}

// ----------------------------------------------------
// Modals
// ----------------------------------------------------
function initModals() {
    document.getElementById('btn-open-loan-modal').addEventListener('click', () => {
        populateBorrowerDropdown();
        openModal('modal-apply-loan');
    });

    document.getElementById('btn-add-borrower').addEventListener('click', () => {
        openModal('modal-add-borrower');
    });
}

function openModal(modalId) {
    const el = document.getElementById(modalId);
    if (el) el.classList.add('open');
}

function closeModal(modalId) {
    const el = document.getElementById(modalId);
    if (el) el.classList.remove('open');
}

// ----------------------------------------------------
// Dashboard & Analytics
// ----------------------------------------------------
async function loadDashboardData() {
    try {
        const res = await fetch(`${API_BASE}/dashboard/stats`);
        const json = await res.json();
        if (!json.success) return;

        const stats = json.data;

        // Metric Counters
        document.getElementById('stat-disbursed').innerText = `$${formatNumber(stats.totalDisbursedAmount)}`;
        document.getElementById('stat-repayments').innerText = `$${formatNumber(stats.totalRepaymentsCollected)}`;
        document.getElementById('stat-outstanding').innerText = `$${formatNumber(stats.totalActiveRemainingBalance)}`;
        document.getElementById('stat-approval-rate').innerText = `${stats.approvalRate}%`;

        document.getElementById('stat-active-count').innerText = `${stats.activeLoans} Active Accounts`;
        document.getElementById('stat-borrowers-count').innerText = `${stats.totalBorrowers} Borrowers registered`;

        // Update badge
        const pendingBadge = document.getElementById('pending-count-badge');
        if (pendingBadge) {
            pendingBadge.innerText = stats.pendingLoans;
            pendingBadge.style.display = stats.pendingLoans > 0 ? 'inline-block' : 'none';
        }

        renderDashboardCharts(stats);
        loadDashboardRecentLoans();
    } catch (err) {
        console.error('Error fetching dashboard stats:', err);
    }
}

function renderDashboardCharts(stats) {
    // 1. Loans by Type Donut
    const typeLabels = Object.keys(stats.loansByType || {});
    const typeValues = Object.values(stats.loansByType || {});

    const ctxType = document.getElementById('loanTypeChart').getContext('2d');
    if (loanTypeChartInstance) loanTypeChartInstance.destroy();

    loanTypeChartInstance = new Chart(ctxType, {
        type: 'doughnut',
        data: {
            labels: typeLabels.length ? typeLabels : ['No Data'],
            datasets: [{
                data: typeValues.length ? typeValues : [1],
                backgroundColor: ['#4f46e5', '#06b6d4', '#10b981', '#f59e0b', '#a855f7'],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'right', labels: { color: '#94a3b8', font: { family: 'Plus Jakarta Sans' } } }
            }
        }
    });

    // 2. Loans by Status Bar
    const statusLabels = Object.keys(stats.loansByStatus || {});
    const statusValues = Object.values(stats.loansByStatus || {});

    const ctxStatus = document.getElementById('loanStatusChart').getContext('2d');
    if (loanStatusChartInstance) loanStatusChartInstance.destroy();

    loanStatusChartInstance = new Chart(ctxStatus, {
        type: 'bar',
        data: {
            labels: statusLabels.length ? statusLabels : ['No Data'],
            datasets: [{
                label: 'Loans Count',
                data: statusValues.length ? statusValues : [0],
                backgroundColor: ['#f59e0b', '#06b6d4', '#10b981', '#f43f5e', '#6366f1'],
                borderRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                x: { ticks: { color: '#94a3b8' }, grid: { display: false } },
                y: { ticks: { color: '#94a3b8', stepSize: 1 }, grid: { color: 'rgba(255,255,255,0.05)' } }
            },
            plugins: {
                legend: { display: false }
            }
        }
    });
}

async function loadDashboardRecentLoans() {
    try {
        const res = await fetch(`${API_BASE}/loans`);
        const json = await res.json();
        if (!json.success) return;

        const tableBody = document.querySelector('#dashboard-recent-loans-table tbody');
        tableBody.innerHTML = '';

        const recent = json.data.slice(0, 5);
        if (recent.length === 0) {
            tableBody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: #64748b;">No loan applications found</td></tr>`;
            return;
        }

        recent.forEach(loan => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>#LN-${loan.id}</strong></td>
                <td>
                    <div style="font-weight: 600;">${escapeHtml(loan.userName)}</div>
                    <div style="font-size: 11px; color: #94a3b8;">${escapeHtml(loan.userEmail)}</div>
                </td>
                <td><span class="pill-tag">${loan.loanType}</span></td>
                <td><strong>$${formatNumber(loan.principalAmount)}</strong></td>
                <td>${loan.interestRate}% (${loan.termMonths} Mo)</td>
                <td><span class="text-success" style="font-weight: 600;">$${formatNumber(loan.monthlyEmi)}</span></td>
                <td>${renderStatusBadge(loan.status)}</td>
                <td>
                    ${loan.status === 'PENDING' ? `
                        <button class="btn btn-sm btn-success" onclick="quickApproveLoan(${loan.id})">Approve</button>
                    ` : `
                        <button class="btn btn-sm btn-outline" onclick="inspectLoanSchedule(${loan.id})">View</button>
                    `}
                </td>
            `;
            tableBody.appendChild(tr);
        });
    } catch (err) {
        console.error('Error fetching recent loans:', err);
    }
}

// ----------------------------------------------------
// Loans & Underwriting
// ----------------------------------------------------
async function loadLoans(statusFilter = 'ALL') {
    try {
        const url = (statusFilter && statusFilter !== 'ALL')
            ? `${API_BASE}/loans?status=${statusFilter}`
            : `${API_BASE}/loans`;

        const res = await fetch(url);
        const json = await res.json();
        if (!json.success) return;

        currentLoans = json.data;
        renderLoansTable(currentLoans);
    } catch (err) {
        console.error('Error loading loans:', err);
    }
}

function renderLoansTable(loans) {
    const tableBody = document.querySelector('#all-loans-table tbody');
    tableBody.innerHTML = '';

    if (!loans || loans.length === 0) {
        tableBody.innerHTML = `<tr><td colspan="10" style="text-align: center; color: #64748b; padding: 30px;">No loans found for selected filter</td></tr>`;
        return;
    }

    loans.forEach(loan => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>#LN-${loan.id}</strong></td>
            <td>
                <div style="font-weight: 600;">${escapeHtml(loan.userName)}</div>
                <div style="font-size: 11px; color: #94a3b8;">${escapeHtml(loan.userEmail)}</div>
            </td>
            <td>${renderCreditScoreBadge(loan.userCreditScore)}</td>
            <td><span class="pill-tag">${loan.loanType}</span></td>
            <td><strong>$${formatNumber(loan.principalAmount)}</strong></td>
            <td>${loan.interestRate}% / ${loan.termMonths} Mo</td>
            <td><strong class="text-info">$${formatNumber(loan.monthlyEmi)}</strong></td>
            <td><strong>$${formatNumber(loan.remainingBalance)}</strong></td>
            <td>${renderStatusBadge(loan.status)}</td>
            <td>
                <div style="display: flex; gap: 6px;">
                    ${loan.status === 'PENDING' ? `
                        <button class="btn btn-sm btn-success" title="Approve" onclick="approveLoanPrompt(${loan.id}, ${loan.interestRate})">
                            <i class="fa-solid fa-check"></i>
                        </button>
                        <button class="btn btn-sm btn-secondary text-danger" title="Reject" onclick="rejectLoanPrompt(${loan.id})">
                            <i class="fa-solid fa-xmark"></i>
                        </button>
                    ` : `
                        <button class="btn btn-sm btn-outline" onclick="inspectLoanSchedule(${loan.id})" title="Inspect Schedule">
                            <i class="fa-solid fa-calendar-days"></i>
                        </button>
                    `}
                </div>
            </td>
        `;
        tableBody.appendChild(tr);
    });
}

function filterLoans(status, btn) {
    document.querySelectorAll('.filter-tabs .tab-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    loadLoans(status);
}

function searchLoansTable() {
    const query = document.getElementById('loan-search-input').value.toLowerCase();
    const filtered = currentLoans.filter(l =>
        l.userName.toLowerCase().includes(query) ||
        l.userEmail.toLowerCase().includes(query) ||
        String(l.id).includes(query) ||
        l.loanType.toLowerCase().includes(query)
    );
    renderLoansTable(filtered);
}

async function quickApproveLoan(loanId) {
    await approveLoanPrompt(loanId, null);
    loadDashboardData();
    loadLoans();
}

async function approveLoanPrompt(loanId, currentRate) {
    const rateInput = prompt(`Approve Loan #LN-${loanId}?\n\nEnter approved interest rate % (or leave blank for ${currentRate || 'standard'}%):`, currentRate || '');
    if (rateInput === null) return; // User cancelled

    const payload = {};
    if (rateInput.trim() !== '') {
        const rate = parseFloat(rateInput);
        if (!isNaN(rate) && rate > 0) {
            payload.approvedInterestRate = rate;
        }
    }

    try {
        const res = await fetch(`${API_BASE}/loans/${loanId}/approve`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const json = await res.json();
        if (json.success) {
            showToast(`Loan #LN-${loanId} approved and activated!`, 'success');
            loadLoans();
            loadDashboardData();
        } else {
            showToast(json.message || 'Failed to approve loan', 'error');
        }
    } catch (err) {
        showToast('Error connecting to server', 'error');
    }
}

async function rejectLoanPrompt(loanId) {
    const reason = prompt(`Reject Loan #LN-${loanId}?\n\nEnter reason for rejection:`, 'Insufficient income / high risk profile');
    if (!reason) return;

    try {
        const res = await fetch(`${API_BASE}/loans/${loanId}/reject`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ reason })
        });
        const json = await res.json();
        if (json.success) {
            showToast(`Loan #LN-${loanId} rejected.`, 'success');
            loadLoans();
            loadDashboardData();
        } else {
            showToast(json.message || 'Failed to reject loan', 'error');
        }
    } catch (err) {
        showToast('Error connecting to server', 'error');
    }
}

// ----------------------------------------------------
// Borrowers Hub
// ----------------------------------------------------
async function loadBorrowers() {
    try {
        const res = await fetch(`${API_BASE}/users`);
        const json = await res.json();
        if (!json.success) return;

        currentBorrowers = json.data;
        renderBorrowersGrid(currentBorrowers);
    } catch (err) {
        console.error('Error loading borrowers:', err);
    }
}

function renderBorrowersGrid(borrowers) {
    const container = document.getElementById('borrowers-cards-container');
    container.innerHTML = '';

    if (!borrowers || borrowers.length === 0) {
        container.innerHTML = `<div class="empty-state" style="grid-column: 1/-1;"><h4>No borrowers registered yet. Click "Register Borrower" to add one.</h4></div>`;
        return;
    }

    borrowers.forEach(b => {
        const initials = b.name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
        const card = document.createElement('div');
        card.className = 'borrower-card';
        card.innerHTML = `
            <div>
                <div class="borrower-header">
                    <div class="avatar-circle">${initials}</div>
                    <div class="borrower-details">
                        <h4>${escapeHtml(b.name)}</h4>
                        <p>${escapeHtml(b.email)}</p>
                    </div>
                </div>

                <div class="borrower-stats-row">
                    <div class="b-stat">
                        <span class="b-lbl">Monthly Income</span>
                        <span class="b-val text-success">$${formatNumber(b.monthlyIncome)}</span>
                    </div>
                    <div class="b-stat">
                        <span class="b-lbl">Credit Rating</span>
                        <span class="b-val">${renderCreditScoreBadge(b.creditScore)}</span>
                    </div>
                    <div class="b-stat">
                        <span class="b-lbl">Active Loans</span>
                        <span class="b-val">${b.activeLoans}</span>
                    </div>
                    <div class="b-stat">
                        <span class="b-lbl">Total Borrowed</span>
                        <span class="b-val text-info">$${formatNumber(b.totalBorrowed)}</span>
                    </div>
                </div>

                <div style="font-size: 11px; color: #94a3b8; margin-bottom: 12px;">
                    <i class="fa-solid fa-briefcase"></i> ${formatEmployment(b.employmentStatus)}
                    ${b.phone ? `<br><i class="fa-solid fa-phone"></i> ${escapeHtml(b.phone)}` : ''}
                </div>
            </div>

            <div style="display: flex; gap: 8px; margin-top: 10px;">
                <button class="btn btn-sm btn-primary btn-block" onclick="applyLoanForUser(${b.id})">
                    <i class="fa-solid fa-plus"></i> Apply Loan
                </button>
            </div>
        `;
        container.appendChild(card);
    });
}

function searchBorrowers() {
    const query = document.getElementById('borrower-search-input').value.toLowerCase();
    const filtered = currentBorrowers.filter(b =>
        b.name.toLowerCase().includes(query) ||
        b.email.toLowerCase().includes(query) ||
        (b.phone && b.phone.includes(query))
    );
    renderBorrowersGrid(filtered);
}

function applyLoanForUser(userId) {
    populateBorrowerDropdown(userId);
    openModal('modal-apply-loan');
}

function populateBorrowerDropdown(selectedId = null) {
    const select = document.getElementById('apply-user-id');
    select.innerHTML = '<option value="">-- Choose registered borrower --</option>';

    currentBorrowers.forEach(b => {
        const opt = document.createElement('option');
        opt.value = b.id;
        opt.innerText = `${b.name} (${b.email}) - Score: ${b.creditScore}`;
        if (selectedId && b.id === selectedId) opt.selected = true;
        select.appendChild(opt);
    });
}

async function submitBorrower(e) {
    e.preventDefault();
    const payload = {
        name: document.getElementById('borrower-name').value,
        email: document.getElementById('borrower-email').value,
        phone: document.getElementById('borrower-phone').value,
        monthlyIncome: parseFloat(document.getElementById('borrower-income').value),
        creditScore: parseInt(document.getElementById('borrower-score').value),
        employmentStatus: document.getElementById('borrower-employment').value,
        address: document.getElementById('borrower-address').value
    };

    try {
        const res = await fetch(`${API_BASE}/users`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const json = await res.json();
        if (json.success) {
            showToast('Borrower registered successfully!', 'success');
            closeModal('modal-add-borrower');
            document.getElementById('form-add-borrower').reset();
            loadBorrowers();
            loadDashboardData();
        } else {
            showToast(json.message || 'Validation failed', 'error');
        }
    } catch (err) {
        showToast('Error registering borrower', 'error');
    }
}

// ----------------------------------------------------
// Loan Application Form & Eligibility
// ----------------------------------------------------
function onLoanTypeChange() {
    const type = document.getElementById('apply-loan-type').value;
    const rateInput = document.getElementById('apply-rate');
    const termInput = document.getElementById('apply-term');

    switch (type) {
        case 'PERSONAL': rateInput.placeholder = '12.5%'; termInput.value = '36'; break;
        case 'HOME': rateInput.placeholder = '8.5%'; termInput.value = '120'; break;
        case 'AUTO': rateInput.placeholder = '9.0%'; termInput.value = '48'; break;
        case 'EDUCATION': rateInput.placeholder = '10.0%'; termInput.value = '60'; break;
        case 'BUSINESS': rateInput.placeholder = '14.0%'; termInput.value = '60'; break;
    }
}

async function checkEligibilityQuick() {
    const userId = document.getElementById('apply-user-id').value;
    const principal = document.getElementById('apply-principal').value;
    const term = document.getElementById('apply-term').value;
    const banner = document.getElementById('eligibility-banner');

    if (!userId || !principal || !term) {
        showToast('Please select borrower, amount, and term first', 'error');
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/loans/eligibility?userId=${userId}&principalAmount=${principal}&termMonths=${term}`);
        const json = await res.json();
        if (!json.success) return;

        const evalData = json.data;
        banner.style.display = 'block';
        banner.className = `eligibility-box ${evalData.eligible ? 'pass' : 'fail'}`;

        banner.innerHTML = `
            <strong><i class="fa-solid ${evalData.eligible ? 'fa-circle-check' : 'fa-triangle-exclamation'}"></i> Automated Underwriting: ${evalData.riskLevel} Risk Profile (${evalData.creditRating})</strong>
            <ul style="margin-top: 6px; padding-left: 18px;">
                ${evalData.underwritingNotes.map(n => `<li>${n}</li>`).join('')}
            </ul>
        `;
    } catch (err) {
        console.error('Eligibility check error:', err);
    }
}

async function submitLoanApplication(e) {
    e.preventDefault();
    const payload = {
        userId: parseInt(document.getElementById('apply-user-id').value),
        loanType: document.getElementById('apply-loan-type').value,
        principalAmount: parseFloat(document.getElementById('apply-principal').value),
        termMonths: parseInt(document.getElementById('apply-term').value),
        purpose: document.getElementById('apply-purpose').value
    };

    const customRate = document.getElementById('apply-rate').value;
    if (customRate && parseFloat(customRate) > 0) {
        payload.customInterestRate = parseFloat(customRate);
    }

    try {
        const res = await fetch(`${API_BASE}/loans/apply`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const json = await res.json();
        if (json.success) {
            showToast('Loan application created successfully!', 'success');
            closeModal('modal-apply-loan');
            document.getElementById('form-apply-loan').reset();
            document.getElementById('eligibility-banner').style.display = 'none';
            loadLoans();
            loadDashboardData();
        } else {
            showToast(json.message || 'Application failed', 'error');
        }
    } catch (err) {
        showToast('Error submitting loan application', 'error');
    }
}

// ----------------------------------------------------
// Repayments & Schedules
// ----------------------------------------------------
async function loadRepaymentLoans() {
    try {
        const res = await fetch(`${API_BASE}/loans`);
        const json = await res.json();
        if (!json.success) return;

        const listContainer = document.getElementById('repayment-loan-list');
        listContainer.innerHTML = '';

        const activeLoans = json.data.filter(l => l.status === 'ACTIVE' || l.status === 'FULLY_PAID' || l.status === 'APPROVED');

        if (activeLoans.length === 0) {
            listContainer.innerHTML = '<div style="color: #64748b; font-size: 13px; text-align: center; padding: 20px;">No active loans available</div>';
            return;
        }

        activeLoans.forEach(loan => {
            const item = document.createElement('div');
            item.className = `loan-picker-item ${selectedLoanForRepayment && selectedLoanForRepayment.id === loan.id ? 'selected' : ''}`;
            item.onclick = () => selectLoanForSchedule(loan.id);
            item.innerHTML = `
                <div class="l-title">
                    <span>#LN-${loan.id} ${escapeHtml(loan.userName)}</span>
                    <span class="text-info">$${formatNumber(loan.remainingBalance)}</span>
                </div>
                <div class="l-sub">
                    <span>${loan.loanType} &bull; EMI: $${formatNumber(loan.monthlyEmi)}</span>
                    <span style="float: right;">${renderStatusBadge(loan.status)}</span>
                </div>
            `;
            listContainer.appendChild(item);
        });

        if (!selectedLoanForRepayment && activeLoans.length > 0) {
            selectLoanForSchedule(activeLoans[0].id);
        }
    } catch (err) {
        console.error('Error loading repayment loans:', err);
    }
}

async function selectLoanForSchedule(loanId) {
    try {
        const res = await fetch(`${API_BASE}/loans/${loanId}`);
        const json = await res.json();
        if (!json.success) return;

        selectedLoanForRepayment = json.data;

        // Highlight selected item in list
        document.querySelectorAll('.loan-picker-item').forEach(el => el.classList.remove('selected'));

        document.getElementById('schedule-empty-state').style.display = 'none';
        document.getElementById('schedule-details-view').style.display = 'block';

        const loan = selectedLoanForRepayment;

        // Banner details
        document.getElementById('loan-detail-banner').innerHTML = `
            <div class="b-stat">
                <span class="b-lbl">Borrower</span>
                <span class="b-val">${escapeHtml(loan.userName)}</span>
            </div>
            <div class="b-stat">
                <span class="b-lbl">Principal Disbursed</span>
                <span class="b-val">$${formatNumber(loan.principalAmount)}</span>
            </div>
            <div class="b-stat">
                <span class="b-lbl">Monthly EMI</span>
                <span class="b-val text-info">$${formatNumber(loan.monthlyEmi)}</span>
            </div>
            <div class="b-stat">
                <span class="b-lbl">Remaining Balance</span>
                <span class="b-val text-warning">$${formatNumber(loan.remainingBalance)}</span>
            </div>
            <div class="b-stat">
                <span class="b-lbl">Paid Installments</span>
                <span class="b-val text-success">${loan.paidInstallments} / ${loan.termMonths}</span>
            </div>
        `;

        // Populate EMI Schedule Table
        const scheduleTbody = document.querySelector('#schedule-table tbody');
        scheduleTbody.innerHTML = '';

        if (loan.emiSchedules && loan.emiSchedules.length > 0) {
            loan.emiSchedules.forEach(s => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td><strong>#${s.installmentNumber}</strong></td>
                    <td>${s.dueDate}</td>
                    <td><strong>$${formatNumber(s.emiAmount)}</strong></td>
                    <td>$${formatNumber(s.principalComponent)}</td>
                    <td>$${formatNumber(s.interestComponent)}</td>
                    <td>$${formatNumber(s.remainingPrincipal)}</td>
                    <td>${renderScheduleStatus(s.status)}</td>
                `;
                scheduleTbody.appendChild(tr);
            });
        }

        // Populate Transaction Ledger Table
        const txTbody = document.querySelector('#loan-tx-table tbody');
        txTbody.innerHTML = '';

        if (loan.recentRepayments && loan.recentRepayments.length > 0) {
            loan.recentRepayments.forEach(tx => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td><code>${tx.transactionReference || 'TXN-GEN'}</code></td>
                    <td>${formatDateTime(tx.paymentDate)}</td>
                    <td><strong class="text-success">+$${formatNumber(tx.amountPaid)}</strong></td>
                    <td><span class="pill-tag">${tx.paymentMethod}</span></td>
                    <td>$${formatNumber(tx.remainingLoanBalance)}</td>
                    <td>${escapeHtml(tx.notes || '-')}</td>
                `;
                txTbody.appendChild(tr);
            });
        } else {
            txTbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: #64748b;">No payment transactions recorded yet</td></tr>`;
        }

        // Setup Pay Button
        document.getElementById('btn-trigger-payment').onclick = () => {
            document.getElementById('pay-loan-id').value = loan.id;
            document.getElementById('pay-loan-display-id').innerText = `#LN-${loan.id} (${loan.userName})`;
            document.getElementById('pay-loan-balance').innerText = `$${formatNumber(loan.remainingBalance)}`;
            document.getElementById('pay-amount').value = Math.min(loan.monthlyEmi, loan.remainingBalance);
            openModal('modal-make-payment');
        };
    } catch (err) {
        console.error('Error selecting loan for schedule:', err);
    }
}

function inspectLoanSchedule(loanId) {
    switchView('repayments');
    selectLoanForSchedule(loanId);
}

async function submitPayment(e) {
    e.preventDefault();
    const loanId = parseInt(document.getElementById('pay-loan-id').value);
    const amount = parseFloat(document.getElementById('pay-amount').value);
    const method = document.getElementById('pay-method').value;
    const notes = document.getElementById('pay-notes').value;

    try {
        const res = await fetch(`${API_BASE}/repayments/pay`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                loanId: loanId,
                amount: amount,
                paymentMethod: method,
                notes: notes
            })
        });
        const json = await res.json();
        if (json.success) {
            showToast(`Repayment of $${formatNumber(amount)} processed successfully!`, 'success');
            closeModal('modal-make-payment');
            document.getElementById('form-make-payment').reset();
            selectLoanForSchedule(loanId);
            loadRepaymentLoans();
            loadDashboardData();
        } else {
            showToast(json.message || 'Payment failed', 'error');
        }
    } catch (err) {
        showToast('Error communicating with server', 'error');
    }
}

// ----------------------------------------------------
// EMI Financial Engine Simulator
// ----------------------------------------------------
async function runCalculator() {
    const principal = parseFloat(document.getElementById('calc-amount').value);
    const rate = parseFloat(document.getElementById('calc-rate').value);
    const term = parseInt(document.getElementById('calc-term').value);

    document.getElementById('calc-amount-val').innerText = `$${formatNumber(principal)}`;
    document.getElementById('calc-rate-val').innerText = `${rate}%`;
    document.getElementById('calc-term-val').innerText = `${term} Months (${(term / 12).toFixed(1)} yrs)`;

    try {
        const res = await fetch(`${API_BASE}/calculator/calculate`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                principalAmount: principal,
                annualInterestRate: rate,
                termMonths: term
            })
        });
        const json = await res.json();
        if (!json.success) return;

        const data = json.data;

        document.getElementById('calc-emi-result').innerText = `$${formatNumber(data.monthlyEmi)}`;
        document.getElementById('calc-interest-result').innerText = `$${formatNumber(data.totalInterest)}`;
        document.getElementById('calc-total-result').innerText = `$${formatNumber(data.totalRepayable)}`;

        renderCalcChart(principal, data.totalInterest);
        renderCalcScheduleTable(data.amortizationSchedule);
    } catch (err) {
        console.error('Calculator error:', err);
    }
}

function renderCalcChart(principal, interest) {
    const ctx = document.getElementById('calcBreakdownChart').getContext('2d');
    if (calcChartInstance) calcChartInstance.destroy();

    calcChartInstance = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Principal Amount', 'Total Interest'],
            datasets: [{
                data: [principal, interest],
                backgroundColor: ['#4f46e5', '#06b6d4'],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'bottom', labels: { color: '#94a3b8' } }
            }
        }
    });
}

function renderCalcScheduleTable(schedule) {
    const tbody = document.querySelector('#calc-schedule-table tbody');
    tbody.innerHTML = '';

    schedule.forEach(s => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>#${s.installmentNumber}</td>
            <td>${s.dueDate}</td>
            <td><strong>$${formatNumber(s.emiAmount)}</strong></td>
            <td class="text-success">$${formatNumber(s.principalComponent)}</td>
            <td class="text-info">$${formatNumber(s.interestComponent)}</td>
            <td>$${formatNumber(s.remainingPrincipal)}</td>
        `;
        tbody.appendChild(tr);
    });
}

function applyWithCalculatorValues() {
    const principal = document.getElementById('calc-amount').value;
    const rate = document.getElementById('calc-rate').value;
    const term = document.getElementById('calc-term').value;

    populateBorrowerDropdown();
    document.getElementById('apply-principal').value = principal;
    document.getElementById('apply-rate').value = rate;
    document.getElementById('apply-term').value = term;

    openModal('modal-apply-loan');
}

// ----------------------------------------------------
// Monthly Expenses & Splitwise Engine
// ----------------------------------------------------
async function loadExpenseData() {
    try {
        // 1. Fetch monthly summary
        const now = new Date();
        const resSum = await fetch(`${API_BASE}/expenses/summary?year=${now.getFullYear()}&month=${now.getMonth() + 1}`);
        const jsonSum = await resSum.json();

        if (jsonSum.success) {
            const summary = jsonSum.data;
            document.getElementById('exp-total-spent').innerText = `$${formatNumber(summary.totalSpent)}`;
            document.getElementById('exp-count-sub').innerText = `${summary.totalExpenseCount} logged in ${summary.monthName}`;
            renderExpenseCharts(summary);
        }

        // 2. Fetch Splitwise Board
        const resBoard = await fetch(`${API_BASE}/expenses/splitwise/board`);
        const jsonBoard = await resBoard.json();

        if (jsonBoard.success) {
            const board = jsonBoard.data;
            document.getElementById('exp-group-volume').innerText = `$${formatNumber(board.totalGroupSpending)}`;
            document.getElementById('exp-unsettled-debt').innerText = `$${formatNumber(board.totalUnsettledDebt)}`;
            document.getElementById('exp-debt-count').innerText = `${board.simplifiedDebts.length} pending settlements`;

            renderSplitwiseBoard(board);
        }

        // 3. Load Expense Table
        loadExpensesTable();
    } catch (err) {
        console.error('Error loading expense data:', err);
    }
}

function renderSplitwiseBoard(board) {
    // 1. Member Net Balances List
    const userContainer = document.getElementById('splitwise-user-balances');
    userContainer.innerHTML = '';

    if (!board.userBalances || board.userBalances.length === 0) {
        userContainer.innerHTML = `<div style="color: #64748b; font-size: 13px;">No member balances</div>`;
    } else {
        board.userBalances.forEach(ub => {
            const item = document.createElement('div');
            item.className = 'user-balance-item';

            let balanceHtml = '';
            if (ub.status === 'OWED') {
                balanceHtml = `<div class="status-owed">+ $${formatNumber(ub.netBalance)}<br><span style="font-size: 10px; color: #34d399; font-weight: normal;">gets back in total</span></div>`;
            } else if (ub.status === 'OWES') {
                balanceHtml = `<div class="status-owes">- $${formatNumber(Math.abs(ub.netBalance))}<br><span style="font-size: 10px; color: #fb7185; font-weight: normal;">owes in total</span></div>`;
            } else {
                balanceHtml = `<div class="status-settled"><i class="fa-solid fa-check"></i> Settled Up</div>`;
            }

            item.innerHTML = `
                <div class="u-info">
                    <h5>${escapeHtml(ub.userName)}</h5>
                    <p>${escapeHtml(ub.userEmail)}</p>
                </div>
                ${balanceHtml}
            `;
            userContainer.appendChild(item);
        });
    }

    // 2. Simplified Pairwise Debts List
    const debtContainer = document.getElementById('splitwise-peer-debts');
    debtContainer.innerHTML = '';

    if (!board.simplifiedDebts || board.simplifiedDebts.length === 0) {
        debtContainer.innerHTML = `<div style="color: #64748b; font-size: 13px; padding: 12px; background: rgba(255,255,255,0.02); border-radius: var(--radius-md);"><i class="fa-solid fa-circle-check text-success"></i> All shared debts are completely settled up!</div>`;
    } else {
        board.simplifiedDebts.forEach(d => {
            const card = document.createElement('div');
            card.className = 'peer-debt-card';
            card.innerHTML = `
                <div class="peer-debt-summary">
                    <i class="fa-solid fa-arrow-right-arrow-left"></i>
                    <div>
                        <div class="peer-debt-text">${escapeHtml(d.debtorName)} &rarr; <span class="text-success">${escapeHtml(d.creditorName)}</span></div>
                        <div style="font-size: 11px; color: #94a3b8;">Direct simplified settlement</div>
                    </div>
                </div>
                <div class="peer-debt-actions">
                    <span style="font-size: 15px; font-weight: 700; color: #f59e0b; margin-right: 8px;">$${formatNumber(d.amount)}</span>
                    <button class="btn btn-sm btn-success" onclick="openSettleModal(${d.debtorId}, ${d.creditorId}, ${d.amount})">
                        <i class="fa-solid fa-check"></i> Settle
                    </button>
                </div>
            `;
            debtContainer.appendChild(card);
        });
    }
}

function renderExpenseCharts(summary) {
    // 1. Spending by Category Chart
    const catLabels = Object.keys(summary.expensesByCategory || {}).map(c => c.replace(/_/g, ' '));
    const catValues = Object.values(summary.expensesByCategory || {});

    const ctxCat = document.getElementById('expenseCategoryChart').getContext('2d');
    if (expenseCategoryChartInstance) expenseCategoryChartInstance.destroy();

    expenseCategoryChartInstance = new Chart(ctxCat, {
        type: 'doughnut',
        data: {
            labels: catLabels.length ? catLabels : ['No Expenses'],
            datasets: [{
                data: catValues.length ? catValues : [1],
                backgroundColor: ['#06b6d4', '#10b981', '#f59e0b', '#a855f7', '#6366f1', '#f43f5e', '#3b82f6'],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'right', labels: { color: '#94a3b8', font: { family: 'Plus Jakarta Sans', size: 11 } } }
            }
        }
    });

    // 2. Spending by Member Chart
    const payerLabels = Object.keys(summary.expensesByPayer || {});
    const payerValues = Object.values(summary.expensesByPayer || {});

    const ctxPayer = document.getElementById('expensePayerChart').getContext('2d');
    if (expensePayerChartInstance) expensePayerChartInstance.destroy();

    expensePayerChartInstance = new Chart(ctxPayer, {
        type: 'bar',
        data: {
            labels: payerLabels.length ? payerLabels : ['No Data'],
            datasets: [{
                label: 'Paid ($)',
                data: payerValues.length ? payerValues : [0],
                backgroundColor: ['#4f46e5', '#06b6d4', '#10b981', '#f59e0b'],
                borderRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                x: { ticks: { color: '#94a3b8' }, grid: { display: false } },
                y: { ticks: { color: '#94a3b8' }, grid: { color: 'rgba(255,255,255,0.05)' } }
            },
            plugins: {
                legend: { display: false }
            }
        }
    });
}

async function loadExpensesTable(categoryFilter = 'ALL') {
    try {
        const url = (categoryFilter && categoryFilter !== 'ALL')
            ? `${API_BASE}/expenses?category=${categoryFilter}`
            : `${API_BASE}/expenses`;

        const res = await fetch(url);
        const json = await res.json();
        if (!json.success) return;

        currentExpenses = json.data;
        const tbody = document.querySelector('#all-expenses-table tbody');
        tbody.innerHTML = '';

        if (!currentExpenses || currentExpenses.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: #64748b; padding: 24px;">No expenses logged for this filter</td></tr>`;
            return;
        }

        currentExpenses.forEach(exp => {
            let splitDetails = '<span style="color: #64748b;">Personal (No split)</span>';
            if (exp.isSplit && exp.splits && exp.splits.length > 0) {
                splitDetails = exp.splits.map(s => `
                    <span class="pill-tag" style="margin: 2px; font-size: 10px;">
                        ${escapeHtml(s.userName)}: $${formatNumber(s.owedAmount)}
                    </span>
                `).join('');
            }

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>${exp.expenseDate}</strong></td>
                <td>
                    <div style="font-weight: 600;">${escapeHtml(exp.description)}</div>
                    ${exp.notes ? `<div style="font-size: 11px; color: #94a3b8;">${escapeHtml(exp.notes)}</div>` : ''}
                </td>
                <td><span class="pill-tag">${exp.category.replace(/_/g, ' ')}</span></td>
                <td><strong class="text-info">${escapeHtml(exp.payerName)}</strong></td>
                <td><span class="status-badge ${exp.isSplit ? 'badge-active-status' : 'badge-pending-status'}">${exp.isSplit ? 'Splitwise Group' : 'Individual'}</span></td>
                <td>${splitDetails}</td>
                <td><strong style="font-size: 14px; color: #fff;">$${formatNumber(exp.amount)}</strong></td>
                <td>
                    <button class="btn btn-sm btn-secondary text-danger" title="Delete" onclick="deleteExpenseItem(${exp.id})">
                        <i class="fa-solid fa-trash"></i>
                    </button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        console.error('Error loading expenses table:', err);
    }
}

function filterExpensesByCategory(cat, btn) {
    document.querySelectorAll('#view-expenses .filter-tabs .tab-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    loadExpensesTable(cat);
}

async function deleteExpenseItem(id) {
    if (!confirm('Are you sure you want to delete this expense and its split records?')) return;

    try {
        const res = await fetch(`${API_BASE}/expenses/${id}`, { method: 'DELETE' });
        const json = await res.json();
        if (json.success) {
            showToast('Expense deleted successfully', 'success');
            loadExpenseData();
        } else {
            showToast(json.message || 'Delete failed', 'error');
        }
    } catch (err) {
        showToast('Error connecting to server', 'error');
    }
}

function openExpenseModal() {
    populateExpensePayerDropdown();
    populateSplitParticipants();
    document.getElementById('exp-date').valueAsDate = new Date();
    updateSplitPreview();
    openModal('modal-add-expense');
}

function populateExpensePayerDropdown() {
    const select = document.getElementById('exp-payer');
    select.innerHTML = '';

    currentBorrowers.forEach(b => {
        const opt = document.createElement('option');
        opt.value = b.id;
        opt.innerText = `${b.name} (${b.email})`;
        select.appendChild(opt);
    });
}

function populateSplitParticipants() {
    const container = document.getElementById('split-participants-checkboxes');
    container.innerHTML = '';

    currentBorrowers.forEach(b => {
        const label = document.createElement('label');
        label.style.display = 'flex';
        label.style.alignItems = 'center';
        label.style.gap = '8px';
        label.style.fontSize = '12px';
        label.style.color = '#cbd5e1';
        label.style.cursor = 'pointer';

        label.innerHTML = `
            <input type="checkbox" class="participant-chk" value="${b.id}" checked onchange="updateSplitPreview()">
            <span>${escapeHtml(b.name)}</span>
        `;
        container.appendChild(label);
    });
}

function toggleSplitSection() {
    const isSplit = document.getElementById('exp-is-split').checked;
    document.getElementById('split-details-section').style.display = isSplit ? 'block' : 'none';
    updateSplitPreview();
}

function updateSplitPreview() {
    const isSplit = document.getElementById('exp-is-split').checked;
    if (!isSplit) {
        document.getElementById('split-calc-preview').innerText = 'Expense paid entirely for personal account.';
        return;
    }

    const amount = parseFloat(document.getElementById('exp-amount').value) || 0;
    const chks = document.querySelectorAll('.participant-chk:checked');
    const count = chks.length;

    if (count === 0 || amount <= 0) {
        document.getElementById('split-calc-preview').innerText = 'Please enter amount and select at least 1 participant.';
        return;
    }

    const share = (amount / count).toFixed(2);
    document.getElementById('split-calc-preview').innerHTML = `
        <i class="fa-solid fa-calculator"></i> Split equally <strong>${count} ways</strong>: <strong>$${share}</strong> per participant.
    `;
}

async function submitExpense(e) {
    e.preventDefault();
    const payerId = parseInt(document.getElementById('exp-payer').value);
    const amount = parseFloat(document.getElementById('exp-amount').value);
    const isSplit = document.getElementById('exp-is-split').checked;

    const payload = {
        description: document.getElementById('exp-desc').value,
        amount: amount,
        category: document.getElementById('exp-category').value,
        expenseDate: document.getElementById('exp-date').value,
        payerId: payerId,
        isSplit: isSplit,
        splitType: 'EQUAL',
        notes: document.getElementById('exp-notes').value,
        splits: []
    };

    if (isSplit) {
        const chks = document.querySelectorAll('.participant-chk:checked');
        const count = chks.length;
        if (count === 0) {
            showToast('Please select at least 1 member to split with', 'error');
            return;
        }

        const share = parseFloat((amount / count).toFixed(2));
        chks.forEach(chk => {
            payload.splits.push({
                userId: parseInt(chk.value),
                owedAmount: share,
                percentage: parseFloat((100 / count).toFixed(2))
            });
        });
    }

    try {
        const res = await fetch(`${API_BASE}/expenses`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const json = await res.json();
        if (json.success) {
            showToast('Expense logged and split calculated successfully!', 'success');
            closeModal('modal-add-expense');
            document.getElementById('form-add-expense').reset();
            loadExpenseData();
        } else {
            showToast(json.message || 'Failed to log expense', 'error');
        }
    } catch (err) {
        showToast('Error communicating with server', 'error');
    }
}

function openSettleModal(debtorId = null, creditorId = null, defaultAmount = null) {
    const payerSelect = document.getElementById('settle-payer');
    const payeeSelect = document.getElementById('settle-payee');

    payerSelect.innerHTML = '';
    payeeSelect.innerHTML = '';

    currentBorrowers.forEach(b => {
        const opt1 = document.createElement('option');
        opt1.value = b.id;
        opt1.innerText = `${b.name}`;
        if (debtorId && b.id === debtorId) opt1.selected = true;
        payerSelect.appendChild(opt1);

        const opt2 = document.createElement('option');
        opt2.value = b.id;
        opt2.innerText = `${b.name}`;
        if (creditorId && b.id === creditorId) opt2.selected = true;
        payeeSelect.appendChild(opt2);
    });

    if (defaultAmount) {
        document.getElementById('settle-amount').value = defaultAmount;
    } else {
        document.getElementById('settle-amount').value = '';
    }

    openModal('modal-settle-debt');
}

async function submitSettlement(e) {
    e.preventDefault();
    const payerId = parseInt(document.getElementById('settle-payer').value);
    const payeeId = parseInt(document.getElementById('settle-payee').value);
    const amount = parseFloat(document.getElementById('settle-amount').value);
    const method = document.getElementById('settle-method').value;
    const notes = document.getElementById('settle-notes').value;

    if (payerId === payeeId) {
        showToast('Payer and Payee cannot be the same borrower', 'error');
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/expenses/splitwise/settle`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                payerId: payerId,
                payeeId: payeeId,
                amount: amount,
                paymentMethod: method,
                notes: notes
            })
        });
        const json = await res.json();
        if (json.success) {
            showToast(`Settlement of $${formatNumber(amount)} recorded!`, 'success');
            closeModal('modal-settle-debt');
            document.getElementById('form-settle-debt').reset();
            loadExpenseData();
        } else {
            showToast(json.message || 'Settlement recording failed', 'error');
        }
    } catch (err) {
        showToast('Error recording settlement', 'error');
    }
}

// ----------------------------------------------------
// UI Formatters & Helpers
// ----------------------------------------------------
function formatNumber(num) {
    if (num === null || num === undefined) return '0';
    return Number(num).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function formatDateTime(dtStr) {
    if (!dtStr) return '-';
    const d = new Date(dtStr);
    return d.toLocaleDateString() + ' ' + d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

function formatEmployment(status) {
    if (!status) return 'Employed';
    return status.replace(/_/g, ' ');
}

function renderStatusBadge(status) {
    switch (status) {
        case 'PENDING':
            return `<span class="status-badge badge-pending-status"><i class="fa-solid fa-clock"></i> Pending</span>`;
        case 'ACTIVE':
        case 'APPROVED':
            return `<span class="status-badge badge-active-status"><i class="fa-solid fa-bolt"></i> Active</span>`;
        case 'FULLY_PAID':
            return `<span class="status-badge badge-paid-status"><i class="fa-solid fa-circle-check"></i> Paid Off</span>`;
        case 'REJECTED':
            return `<span class="status-badge badge-rejected-status"><i class="fa-solid fa-ban"></i> Rejected</span>`;
        default:
            return `<span class="status-badge">${status}</span>`;
    }
}

function renderScheduleStatus(status) {
    switch (status) {
        case 'PAID':
            return `<span class="status-badge badge-paid-status"><i class="fa-solid fa-check"></i> Paid</span>`;
        case 'OVERDUE':
            return `<span class="status-badge badge-rejected-status"><i class="fa-solid fa-circle-exclamation"></i> Overdue</span>`;
        default:
            return `<span class="status-badge badge-pending-status">Pending</span>`;
    }
}

function renderCreditScoreBadge(score) {
    if (!score) score = 650;
    if (score >= 750) return `<span class="credit-badge credit-excellent">${score} (Prime)</span>`;
    if (score >= 670) return `<span class="credit-badge credit-good">${score} (Good)</span>`;
    if (score >= 580) return `<span class="credit-badge credit-fair">${score} (Fair)</span>`;
    return `<span class="credit-badge credit-poor">${score} (Subprime)</span>`;
}

function showToast(message, type = 'success') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <i class="fa-solid ${type === 'success' ? 'fa-circle-check text-success' : 'fa-triangle-exclamation text-danger'}"></i>
        <span>${escapeHtml(message)}</span>
    `;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
}


