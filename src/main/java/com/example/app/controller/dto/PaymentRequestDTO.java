package com.example.app.controller.dto;

import com.example.app.model.CreditCard;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {
    private Long orderID;
    private CreditCard creditCard;
}
