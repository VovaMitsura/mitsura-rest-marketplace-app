package com.example.app.service.stripe;

import com.example.app.controller.dto.ProductDTO;
import com.example.app.exception.ApplicationExceptionHandler;
import com.example.app.exception.PaymentException;
import com.example.app.exception.ResourceConflictException;
import com.example.app.model.CreditCard;
import com.example.app.model.Order;
import com.example.app.model.OrderDetails;
import com.example.app.model.Product;
import com.example.app.repository.OrderRepository;
import com.example.app.service.PaymentProvider;
import com.example.app.service.ProductService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Token;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
class StripePaymentService implements PaymentProvider {

    private final ProductService productService;
    private final OrderRepository orderRepository;

    public StripePaymentService(ProductService productService, @Value("${stripe.secret.key}") String stripeApiKey, OrderRepository orderRepository) {
        this.productService = productService;
        this.orderRepository = orderRepository;
        Stripe.apiKey = stripeApiKey;
    }

    @Override
    public Charge pay(CreditCard card, Order order) {

        List<OrderDetails> orderDetails = order.getOrderDetails();

        for (OrderDetails details : orderDetails) {
            Product ordederProduct = details.getProduct();
            Product productInMarket = productService.getProductById(ordederProduct.getId());

            if (details.getQuantity() > productInMarket.getQuantity()) {
                throw new ResourceConflictException(ApplicationExceptionHandler.QUANTITY_CONFLICT,
                        String.format("There are not so quantity [%d] goods [%s] " + "in market",
                                ordederProduct.getQuantity(), ordederProduct.getName()));
            }
        }

        Charge charge = chargeCreditCard(card, order);

        for (OrderDetails details : orderDetails) {
            Product ordederProduct = details.getProduct();
            Product productInMarket = productService.getProductById(ordederProduct.getId());

            ProductDTO productUpdateQuantity = new ProductDTO();
            productUpdateQuantity.setName(productInMarket.getName());
            productUpdateQuantity.setPrice(productInMarket.getPrice());
            if (productInMarket.getDiscount() == null) {
                productUpdateQuantity.setDiscount(null);
            } else
                productUpdateQuantity.setDiscount(productInMarket.getDiscount().getName());
            productUpdateQuantity.setCategory(productInMarket.getCategory().getName());
            productUpdateQuantity.setQuantity(productInMarket.getQuantity() - details.getQuantity());


            productService.update(productInMarket.getId(), productUpdateQuantity, productInMarket.getSeller().getEmail());
        }

        order.setStatus(Order.Status.BOUGHT);
        orderRepository.save(order);

        return charge;
    }

    private Charge chargeCreditCard(CreditCard card, Order order) {

        String stripeToken = createStripeToken(card);

        Map<String, Object> chargeParams = new HashMap<>();
        Map<String, Object> metaParams = new HashMap<>();
        chargeParams.put("amount", order.getTotalAmount() * 100);
        chargeParams.put("currency", "USD");
        chargeParams.put("source", stripeToken);
        metaParams.put("order_id", order.getId());
        chargeParams.put("metadata", metaParams);

        try {
            return Charge.create(chargeParams);
        } catch (StripeException e) {
            throw new PaymentException(ApplicationExceptionHandler.PAYMENT_EXCEPTION,
                    String.format("Exception occur during paying for order [%d] with error [%s]",
                            order.getId(), e.getMessage()));
        }
    }

    private String createStripeToken(CreditCard card) {

        Map<String, Object> cardParams = new HashMap<>();

        cardParams.put("number", card.getNumber());
        cardParams.put("exp_month", card.getExpirationMonth());
        cardParams.put("exp_year", card.getExpirationYear());
        cardParams.put("cvc", card.getCvc());

        Map<String, Object> params = new HashMap<>();
        params.put("card", cardParams);

        Token token;

        //Add exceptionTokenCreation
        try {
            token = Token.create(params);
        } catch (StripeException e) {
            throw new IllegalStateException(e);
        }

        return token.getId();
    }

}
