package com.example.app.service;

import com.example.app.model.CreditCard;
import com.example.app.model.Order;
import com.stripe.model.Charge;

public interface PaymentProvider {
    Charge pay(CreditCard card, Order order);
}
