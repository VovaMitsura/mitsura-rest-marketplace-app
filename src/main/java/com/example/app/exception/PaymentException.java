package com.example.app.exception;

public class PaymentException extends RuntimeException{

    private final String errorCode;

    public PaymentException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
