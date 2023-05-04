package com.example.app.service;

import com.example.app.controller.dto.CreateOrderDTO;
import com.example.app.controller.dto.ProductDTO;
import com.example.app.controller.dto.UpdateProductDTO;
import com.example.app.exception.ApplicationExceptionHandler;
import com.example.app.exception.NotFoundException;
import com.example.app.exception.ResourceConflictException;
import com.example.app.model.*;
import com.example.app.repository.OrderDetailsRepository;
import com.example.app.repository.OrderRepository;
import com.example.app.service.stripe.StripePaymentStatus;
import com.stripe.model.Charge;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {OrderService.class})
class OrderServiceTest {

    @Autowired
    OrderService orderService;

    @MockBean
    OrderRepository orderRepository;
    @MockBean
    ProductService productService;
    @MockBean
    UserService userService;
    @MockBean
    OrderDetailsRepository orderDetailsRepository;
    @MockBean
    PaymentProvider paymentService;

    Product product;
    OrderDetails orderDetails;
    Order order;
    User user = new User(1L, "John", "John@mail.com", User.Role.SELLER, null,
            null, null, null);
    CreditCard card = new CreditCard("12345678910", "2024", "4", "123");

    @BeforeEach()
    void setUp() {
        product = new Product(1L, "Honor h2", 225, null,
                new Category(1L, "laptop", "gadget", null),
                user,
                10, null);
        orderDetails = new OrderDetails(1L, product, "Honor h2", null, 1);
        order = new Order(1L, null, 225, null, List.of(orderDetails), Order.Status.CREATED);
    }

    @Test
    void payForOrderWithValidDataShouldReturnPaymentStatusOk() {
        Charge charge = new Charge();
        charge.setStatus("succeeded");
        charge.setId("123");
        charge.setAmount(100000L);
        StripePaymentStatus paymentStatus = new StripePaymentStatus(charge);

        Mockito.when(productService.getProductById(product.getId())).thenReturn(product);
        Mockito.when(paymentService.pay(card, order))
                .thenReturn(charge);
        Mockito.when(productService.update(product.getId(), new ProductDTO(product.getName(), product.getPrice(),
                        null, product.getCategory().getName(),
                        product.getQuantity() - orderDetails.getQuantity()), null))
                .thenReturn(product);
        Mockito.when(orderRepository.save(Mockito.any(Order.class)))
                .thenReturn(order);

        StripePaymentStatus response = (StripePaymentStatus) orderService.payForOrder(card, order);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(paymentStatus.getCharge().getStatus(), response.getCharge().getStatus());
        Assertions.assertEquals(paymentStatus.getCharge().getId(), response.getCharge().getId());
        Assertions.assertEquals(paymentStatus.isSucceeded(), response.isSucceeded());
    }

    @Test
    void payForOrderWithOrderGreaterProductQuantityThrowException() {
        orderDetails.setQuantity(11);
        order.setOrderDetails(List.of(orderDetails));
        Mockito.when(productService.getProductById(product.getId())).thenReturn(product);

        ResourceConflictException exception = Assertions.assertThrows(ResourceConflictException.class,
                () -> orderService.payForOrder(card, order));

        Assertions.assertEquals(ApplicationExceptionHandler.QUANTITY_CONFLICT, exception.getErrorCode());
        Assertions.assertEquals(String.format("There are not so quantity [%d] goods [%s] " + "in market",
                product.getQuantity(), product.getName()), exception.getMessage());
    }

    @Test
    void getUsersOrdersShouldReturnList() {
        Mockito.when(orderRepository.findAllByCustomerEmail(user.getEmail()))
                .thenReturn(List.of(order));

        List<Order> orders = orderService.getUserOrders(user.getEmail());

        Assertions.assertNotNull(orders);
        Assertions.assertEquals(List.of(order), orders);
    }

    @Test
    void getUserWithNoOrdersShouldThrowException() {
        Mockito.when(orderRepository.findAllByCustomerEmail(user.getEmail()))
                .thenReturn(Collections.emptyList());

        NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> orderService.getUserOrders(user.getEmail()));

        Assertions.assertEquals(ApplicationExceptionHandler.ORDER_NOT_FOUND, exception.getErrorCode());
        Assertions.assertEquals(String.format("No order of user [%s]", user.getEmail()), exception.getMessage());
    }

    @Test
    void getUserOrderByIdShouldReturnOneValue() {
        Mockito.when(orderRepository.findByIdAndCustomerEmail(order.getId(), user.getEmail()))
                .thenReturn(Optional.of(order));

        Order userOrder = orderService.getUserOrderById(order.getId(), user.getEmail());

        Assertions.assertNotNull(userOrder);
        Assertions.assertEquals(orderDetails.getQuantity(), userOrder.getOrderDetails().get(0).getQuantity());
        Assertions.assertEquals(orderDetails.getProductName(), userOrder.getOrderDetails().get(0).getProductName());
        Assertions.assertEquals(orderDetails.getId(), userOrder.getOrderDetails().get(0).getId());
        Assertions.assertEquals(order.getId(), userOrder.getId());
        Assertions.assertEquals(order, userOrder);
    }

    @Test
    void getUserWithNoOrderShouldThrowException() {
        Mockito.when(orderRepository.findByIdAndCustomerEmail(order.getId(), user.getEmail()))
                .thenReturn(Optional.empty());

        NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> orderService.getUserOrderById(order.getId(), user.getEmail()));

        Assertions.assertEquals(ApplicationExceptionHandler.ORDER_NOT_FOUND, exception.getErrorCode());
        Assertions.assertEquals(String.format("No order of user [%s] with id [%d]", user.getEmail(), order.getId()),
                exception.getMessage());
    }

    @Test
    void createNewOrderShouldReturnOrder() {
        CreateOrderDTO orderDTO = CreateOrderDTO.builder()
                .orderId(order.getId())
                .productId(product.getId())
                .quantity(10)
                .build();
        order.getOrderDetails().get(0).setQuantity(orderDTO.getQuantity());


        Mockito.when(userService.getUserByEmail(user.getEmail()))
                .thenReturn(user);
        Mockito.when(productService.getProductById(product.getId()))
                .thenReturn(product);
        Mockito.when(orderRepository.findByIdAndCustomerEmail(order.getId(), user.getEmail()))
                .thenReturn(Optional.empty());
        Mockito.when(orderRepository.save(Mockito.any(Order.class)))
                .thenReturn(order);
        Mockito.when(orderDetailsRepository.save(orderDetails))
                .thenReturn(orderDetails);
        Mockito.when(orderDetailsRepository.findAllByOrderId(order.getId()))
                .thenReturn(List.of(orderDetails));

        Order response = orderService.createNewOrder(orderDTO, user.getEmail());

        Assertions.assertNotNull(response);
        Assertions.assertEquals(orderDTO.getOrderId(), response.getId());
        Assertions.assertEquals(orderDTO.getProductId(), response.getOrderDetails().get(0).getProduct().getId());
        Assertions.assertEquals(orderDTO.getQuantity(), response.getOrderDetails().get(0).getQuantity());
        Assertions.assertEquals(Order.Status.CREATED, order.getStatus());
    }

    @Test
    void updateProductShouldReturnNewProduct() {
        UpdateProductDTO productDTO = UpdateProductDTO.builder()
                .name(product.getName())
                .quantity(10)
                .build();
        orderDetails.setQuantity(product.getQuantity());
        Order updateOrder = new Order(order.getId(), order.getCustomer(),
                productDTO.getQuantity() * product.getPrice(),
                order.getDate(), List.of(orderDetails), order.getStatus());

        Mockito.when(orderRepository.findByIdAndCustomerEmail(order.getId(), user.getEmail()))
                .thenReturn(Optional.of(order));
        Mockito.when(orderRepository.save(updateOrder))
                .thenReturn(updateOrder);

        Order response = orderService.updateProduct(order.getId(), user.getEmail(), productDTO);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(updateOrder.getTotalAmount(), response.getTotalAmount());
    }

    @Test
    void updateNonExistingProductInOrderThrowException() {
        UpdateProductDTO productDTO = UpdateProductDTO.builder()
                .name("bla-bla")
                .quantity(10)
                .build();

        Mockito.when(orderRepository.findByIdAndCustomerEmail(order.getId(), user.getEmail()))
                .thenReturn(Optional.of(order));

        NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> orderService.updateProduct(order.getId(), user.getEmail(), productDTO));

        Assertions.assertEquals(ApplicationExceptionHandler.PRODUCT_NOT_FOUND, exception.getErrorCode());
        Assertions.assertEquals(String.format("No product with name [%s] in order with id [%d] of user [%s]",
                productDTO.getName(), order.getId(), user.getEmail()), exception.getMessage());
    }
}