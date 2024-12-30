package com.ing.loan.exception;

/**
 * @author suleyman.yildirim
 */
public class LoanNotFoundException extends BaseException{
    public LoanNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public LoanNotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
