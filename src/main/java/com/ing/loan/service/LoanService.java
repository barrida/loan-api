package com.ing.loan.service;

import com.ing.loan.entity.Loan;
import com.ing.loan.entity.LoanInstallment;
import com.ing.loan.request.LoanRequest;
import com.ing.loan.response.LoanResponse;

import java.util.List;

/**
 * Service interface for Loan operations.
 */
public interface LoanService {
    LoanResponse createLoan(LoanRequest loanRequest);

    List<Loan> listLoansByCustomer(Long customerId);

    List<LoanInstallment> listInstallmentsByLoan(Long loanId);
}
