package com.ing.loan.exception;

/**
 * @author Suleyman Yildirim
 */

public class LoanInstallmentNotFoundException extends BaseException{

    public LoanInstallmentNotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

}
