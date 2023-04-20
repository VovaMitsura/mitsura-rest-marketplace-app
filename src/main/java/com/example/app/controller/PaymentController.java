package com.example.app.controller;


import com.example.app.controller.dto.PaymentRequestDTO;
import com.example.app.controller.dto.PaymentResponseDTO;
import com.example.app.model.Order;
import com.example.app.security.util.UserPrincipalUtil;
import com.example.app.service.OrderService;
import com.example.app.service.PaymentProvider;
import com.stripe.model.Charge;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
class PaymentController {
    private final PaymentProvider paymentProvider;
    private final OrderService orderService;

    PaymentController(PaymentProvider paymentProvider, OrderService orderService) {
        this.paymentProvider = paymentProvider;
        this.orderService = orderService;
    }

    @PostMapping("/pay")
    public ResponseEntity<PaymentResponseDTO> payForOrder(@RequestBody PaymentRequestDTO paymentRequest) {

        String userEmail = UserPrincipalUtil.extractUserEmail();

        Order order = orderService.getUserOrderById(paymentRequest.getOrderID(), userEmail);

        Charge payed = paymentProvider.pay(paymentRequest.getCreditCard(), order);

        PaymentResponseDTO response = PaymentResponseDTO.builder()
                .status(payed.getStatus())
                .order(order)
                .amount(payed.getAmount())
                .build();

        return ResponseEntity.ok(response);
    }

}