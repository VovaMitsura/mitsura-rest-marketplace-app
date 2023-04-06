  package com.example.app.controller;

import com.example.app.controller.dto.OrderCreationResponseDTO;
import com.example.app.controller.dto.ProductToOrderDTO;
import com.example.app.model.Order;
import com.example.app.security.util.UserPrincipalUtil;
import com.example.app.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  @GetMapping()
  @PreAuthorize("hasAnyAuthority('CUSTOMER', 'SELLER', 'ADMIN')")
  public ResponseEntity<List<Order>> getUserOrder() {

    List<Order> userOrders = orderService.getUserOrders(UserPrincipalUtil.extractUserEmail());

    return ResponseEntity.ok(userOrders);
  }

  @PostMapping()
  @PreAuthorize("hasAnyAuthority('CUSTOMER', 'SELLER', 'ADMIN')")
  public ResponseEntity<OrderCreationResponseDTO> createNewOrder(
    @Valid @RequestBody ProductToOrderDTO product) {

    Order newOrder = orderService.createNewOrder(product,
        UserPrincipalUtil.extractUserEmail());

    OrderCreationResponseDTO response = new OrderCreationResponseDTO(
        OrderCreationResponseDTO.DEFAULT_SUCCESSFUL_MESSAGE, newOrder);

    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

}
