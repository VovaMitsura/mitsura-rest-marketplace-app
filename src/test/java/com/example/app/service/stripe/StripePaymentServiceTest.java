package com.example.app.service.stripe;

import com.example.app.exception.ApplicationExceptionHandler;
import com.example.app.exception.PaymentException;
import com.example.app.model.CreditCard;
import com.example.app.model.Order;
import com.example.app.model.OrderDetails;
import com.example.app.model.Product;
import com.example.app.repository.OrderRepository;
import com.example.app.service.ProductService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {StripePaymentService.class})
class StripePaymentServiceTest {

    @Autowired
    StripePaymentService stripePaymentService;
    @MockBean
    ProductService productService;
    @MockBean
    OrderRepository orderRepository;

    StripePaymentService spyStripe;

    Product product = new Product(1L, "Honor h2", 225, null, null,
            null, null, 10, null);
    OrderDetails orderDetails = new OrderDetails(1L, product, "Honor h2", null, 1);
    Order order = new Order(1L, null, 225, null, List.of(orderDetails), Order.Status.CREATED);
    CreditCard card = new CreditCard("12345678910", "2024", "4", "123");


    @BeforeEach
    void setUp() {
        spyStripe = Mockito.spy(stripePaymentService);
    }

    @Test
    void payWithInvalidCardShouldThrowException() {

        Mockito.when(productService.getProductById(product.getId())).thenReturn(product);
        product.setQuantity(product.getQuantity() - orderDetails.getQuantity());

        card.setExpirationYear("-1");

        PaymentException exception = Assertions.assertThrows(PaymentException.class, () -> spyStripe.pay(card, order));

        Assertions.assertEquals(ApplicationExceptionHandler.TOKEN_EXCEPTION, exception.getErrorCode());
    }

}