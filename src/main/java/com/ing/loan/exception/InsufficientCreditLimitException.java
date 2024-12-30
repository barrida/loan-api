package com.ing.loan.exception;

/**
 * @author Suleyman Yildirim
 */
public class InsufficientCreditLimitException extends BaseException {
    public InsufficientCreditLimitException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
//    public InsufficientCreditException() {
//        super(ErrorCode.INSUFFICIENT_CREDIT, "Insufficient credit for loan request");
//    }
}
