package com.ing.loan.service;

import com.ing.loan.response.LoanPaymentResponse;

import java.math.BigDecimal;

/**
 * @author Suleyman Yildirim
 */

public interface PaymentService {
    LoanPaymentResponse payLoan(Long loanId, BigDecimal amount);
}
