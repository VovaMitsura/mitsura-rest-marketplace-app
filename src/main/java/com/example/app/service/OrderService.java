package com.example.app.service;

import com.example.app.controller.dto.ProductToOrderDTO;
import com.example.app.exception.ApplicationExceptionHandler;
import com.example.app.exception.NotFoundException;
import com.example.app.model.Order;
import com.example.app.model.Order.Status;
import com.example.app.model.OrderDetails;
import com.example.app.model.Product;
import com.example.app.model.User;
import com.example.app.repository.OrderDetailsRepository;
import com.example.app.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final UserService userService;
    private final OrderDetailsRepository orderDetailsRepository;

    public List<Order> getUserOrders(String userEmail) {
        List<Order> userOrders = orderRepository.findAllByCustomerEmail(userEmail);

        if (userOrders.isEmpty()) {
            throw new NotFoundException(ApplicationExceptionHandler.ORDER_NOT_FOUND,
                    String.format("No order of user [%s]", userEmail));
        }

        return userOrders;
    }

    public Order createNewOrder(ProductToOrderDTO productToOrder, String userEmail) {

        User customerByEmail = userService.getUserByEmail(userEmail);

        Product product = productService.getProductById(productToOrder.getProductId());

        Order order;

        Optional<Order> optionalOrder = orderRepository.findByIdAndCustomerEmail(productToOrder.getOrderId(), userEmail);
        order = optionalOrder.orElseGet(() -> Order.builder().
                customer(customerByEmail)
                .date(new Timestamp(new Date().getTime()))
                .status(Status.CREATED)
                .build());

        OrderDetails newOrderDetails = OrderDetails.builder()
                .order(order)
                .product(product)
                .quantity(productToOrder.getQuantity())
                .build();

        order = orderRepository.save(order);
        orderDetailsRepository.save(newOrderDetails);
        order.setOrderDetails(orderDetailsRepository.findAllByOrderId(order.getId()));

        return order;
    }
}
