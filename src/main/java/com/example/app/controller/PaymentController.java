package com.example.app.controller;


import com.example.app.controller.dto.PaymentRequestDTO;
import com.example.app.controller.dto.PaymentStatus;
import com.example.app.controller.dto.StipePaymentResponseDTO;
import com.example.app.model.Order;
import com.example.app.security.util.UserPrincipalUtil;
import com.example.app.service.OrderService;
import com.example.app.service.stripe.StripePaymentStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pay")
class PaymentController {
    private final OrderService orderService;

    PaymentController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping()
    public ResponseEntity<StipePaymentResponseDTO> payForOrder(@RequestBody PaymentRequestDTO paymentRequest) throws JsonProcessingException {

        String userEmail = UserPrincipalUtil.extractUserEmail();

        Order order = orderService.getUserOrderById(paymentRequest.getOrderID(), userEmail);

        PaymentStatus paymentStatus = orderService.payForOrder(paymentRequest.getCreditCard(), order);

        StipePaymentResponseDTO response = new StipePaymentResponseDTO((StripePaymentStatus) paymentStatus,
        String.format("Payment is %s", paymentStatus.isSucceeded() ? "succeed" : "fail"));

        return ResponseEntity.ok(response);
    }

}