package com.example.app.controller.dto;

import com.example.app.model.Order;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponseDTO {

    private String status;
    private Order order;
    private Long amount;

}
