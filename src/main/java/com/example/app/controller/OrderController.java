package com.example.app.controller;

import com.example.app.controller.dto.DeleteProductDTO;
import com.example.app.controller.dto.OrderCreationResponseDTO;
import com.example.app.controller.dto.CreateOrderDTO;
import com.example.app.controller.dto.UpdateProductDTO;
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
@PreAuthorize("hasAnyAuthority('CUSTOMER', 'SELLER', 'ADMIN')")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping()
    public ResponseEntity<List<Order>> getUserOrders() {

        List<Order> userOrders = orderService.getUserOrders(UserPrincipalUtil.extractUserEmail());

        return ResponseEntity.ok(userOrders);
    }

    @PostMapping()
    public ResponseEntity<OrderCreationResponseDTO> createNewOrder(
            @Valid @RequestBody CreateOrderDTO product) {

        Order newOrder = orderService.createNewOrder(product,
                UserPrincipalUtil.extractUserEmail());

        OrderCreationResponseDTO response = new OrderCreationResponseDTO(
                OrderCreationResponseDTO.DEFAULT_SUCCESSFUL_MESSAGE, newOrder);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getUserOrder(@PathVariable Long id) {
        String userEmail = UserPrincipalUtil.extractUserEmail();

        Order userOrder = orderService.getUserOrderById(id, userEmail);

        return ResponseEntity.ok(userOrder);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> updateUserOrder(@PathVariable Long id,
                                                 @Valid @RequestBody UpdateProductDTO updateProduct) {

        String userEmail = UserPrincipalUtil.extractUserEmail();
        Order order = orderService.updateProduct(id, userEmail, updateProduct);

        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Order> deleteOrder(@PathVariable Long id) {
        String userEmail = UserPrincipalUtil.extractUserEmail();
        Order order = orderService.deleteOrder(id, userEmail);

        return ResponseEntity.ok(order);
    }

    @PostMapping("/{id}")
    public ResponseEntity<Order> deleteProductFromOrder(@PathVariable Long id,
                                                        @Valid @RequestBody DeleteProductDTO deleteProduct) {
        String userEmail = UserPrincipalUtil.extractUserEmail();
        Order order = orderService.deleteProductInOrder(id, userEmail, deleteProduct);

        return ResponseEntity.ok(order);
    }

}
