package com.example.app.service.stripe;

import com.example.app.exception.ApplicationExceptionHandler;
import com.example.app.exception.PaymentException;
import com.example.app.model.CreditCard;
import com.example.app.model.Order;
import com.example.app.service.PaymentProvider;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Token;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
public class StripePaymentService implements PaymentProvider {

    public StripePaymentService(@Value("${stripe.secret.key}") String stripeApiKey) {
        Stripe.apiKey = stripeApiKey;
    }

    @Override
    public Charge pay(CreditCard card, Order order) {
        return chargeCreditCard(card, order);
    }

    private Charge chargeCreditCard(CreditCard card, Order order) {

        String stripeToken = createStripeToken(card);

        Map<String, Object> chargeParams = new HashMap<>();
        Map<String, Object> metaParams = new HashMap<>();
        chargeParams.put("amount", order.getTotalAmount() * 100);
        chargeParams.put("currency", "USD");
        chargeParams.put("source", stripeToken);
        metaParams.put("order_id", order.getId());
        chargeParams.put("metadata", metaParams);

        try {
            return Charge.create(chargeParams);
        } catch (StripeException e) {
            throw new PaymentException(ApplicationExceptionHandler.PAYMENT_EXCEPTION,
                    String.format("Exception occur during paying for order [%d] with error [%s]",
                            order.getId(), e.getMessage()));
        }
    }

    private String createStripeToken(CreditCard card) throws PaymentException {

        Map<String, Object> cardParams = new HashMap<>();

        cardParams.put("number", card.getNumber());
        cardParams.put("exp_month", card.getExpirationMonth());
        cardParams.put("exp_year", card.getExpirationYear());
        cardParams.put("cvc", card.getCvc());

        Map<String, Object> params = new HashMap<>();
        params.put("card", cardParams);

        Token token;

        try {
            token = Token.create(params);
        } catch (StripeException e) {
            throw new PaymentException(ApplicationExceptionHandler.TOKEN_EXCEPTION,
                    String.format("Exception during stripe token creation [%s]", e.getMessage()));
        }

        return token.getId();
    }

}
