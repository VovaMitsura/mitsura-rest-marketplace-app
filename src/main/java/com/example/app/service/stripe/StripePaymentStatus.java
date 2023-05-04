package com.example.app.service.stripe;

import com.example.app.controller.dto.PaymentStatus;
import com.stripe.model.Charge;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StripePaymentStatus extends PaymentStatus {
    private static final String STATUS_SUCCEED = "succeeded";
    private final Charge charge;

    public StripePaymentStatus(Charge charge) {
        super(charge.getId(), STATUS_SUCCEED.equals(charge.getStatus()));
        if (!this.isSucceeded()) {
            this.errorKey = charge.getFailureCode();
            this.errorMessage = charge.getFailureMessage();
        }

        this.charge = charge;
    }
}
