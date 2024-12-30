package com.ing.loan.exception;

import lombok.Getter;

/**
 * @author suleyman.yildirim
 */
@Getter
public enum ErrorCode {

    LOAN_NOT_FOUND("LOAN_NOT_FOUND"),
    LOAN_INSTALLMENT_NOT_FOUND("LOAN_INSTALLMENT_NOT_FOUND"),
    CUSTOMER_EXISTS("CUSTOMER_EXISTS"),
    CUSTOMER_NOT_FOUND("CUSTOMER_NOT_FOUND"),
    INSUFFICIENT_CREDIT("INSUFFICIENT_CREDIT");

    private final String code;

    ErrorCode(String code) {
        this.code = code;

    }

}

