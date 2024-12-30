package com.ing.loan.exception;

/**
 * @author suleyman.yildirim
 */
public class CustomerNotFoundException extends BaseException{
    public CustomerNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public CustomerNotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public CustomerNotFoundException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

}
