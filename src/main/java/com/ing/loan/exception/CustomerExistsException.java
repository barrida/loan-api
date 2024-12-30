package com.ing.loan.exception;

/**
 * @author suleyman.yildirim
 */
public class CustomerExistsException extends BaseException{
    public CustomerExistsException(ErrorCode errorCode) {
        super(errorCode);
    }

    public CustomerExistsException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public CustomerExistsException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

}
