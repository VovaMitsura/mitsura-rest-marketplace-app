package com.example.app.controller.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PaymentStatus {
    private final String id;
    private final boolean succeeded;
    protected String errorKey;
    protected String errorMessage;

    public PaymentStatus(String id, boolean succeeded) {
        this.id = id;
        this.succeeded = succeeded;
        this.errorKey = null;
        this.errorMessage = null;
    }

    public PaymentStatus(String id, String errorKey, String errorMessage) {
        this.id = id;
        this.succeeded = false;
        this.errorKey = errorKey;
        this.errorMessage = errorMessage;
    }
}
