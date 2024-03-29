package com.example.app.service;

import com.example.app.controller.dto.*;
import com.example.app.exception.ApplicationExceptionHandler;
import com.example.app.exception.NotFoundException;
import com.example.app.exception.ResourceConflictException;
import com.example.app.model.*;
import com.example.app.model.Order.Status;
import com.example.app.repository.OrderDetailsRepository;
import com.example.app.repository.OrderRepository;
import com.example.app.service.stripe.StripePaymentStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    public static final String MAIL_TEMPLATE_ORDER_IS_PAYED = "order_is_payed";
    public static final String MAIL_TEMPLATE_ORDER_IS_NOT_PAYED = "order_is_not_payed";

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final UserService userService;
    private final OrderDetailsRepository orderDetailsRepository;
    private final PaymentProvider paymentService;
    private final MailingService emailService;

    private final Logger logger = LoggerFactory.getLogger(OrderService.class);


    public List<Order> getUserOrders(String userEmail, Order.Status status) {
        List<Order> userOrders = orderRepository.findAllByCustomerEmailAndStatus(userEmail, status);

        if (userOrders.isEmpty()) {
            throw new NotFoundException(ApplicationExceptionHandler.ORDER_NOT_FOUND,
                    String.format("No order of user [%s]", userEmail));
        }

        return userOrders;
    }

    public Order getUserOrderById(Long id, String customerEmail) {
        return orderRepository.findByIdAndCustomerEmailAndStatus(id, customerEmail, Status.CREATED).orElseThrow(() ->
                new NotFoundException(ApplicationExceptionHandler.ORDER_NOT_FOUND,
                        String.format("No order of user [%s] with id [%d]", customerEmail, id)));
    }

    public Order createNewOrder(CreateOrderDTO productToOrder, String userEmail) {

        User customerByEmail = userService.getUserByEmail(userEmail);

        Product product = productService.getProductById(productToOrder.getProductId());

        Order order;

        Optional<Order> optionalOrder = orderRepository.findByIdAndCustomerEmailAndStatus(productToOrder.getOrderId(),
                userEmail, Status.CREATED);
        order = optionalOrder.orElseGet(() -> Order.builder().
                customer(customerByEmail)
                .status(Status.CREATED)
                .build());

        OrderDetails newOrderDetails = OrderDetails.builder()
                .order(order)
                .product(product)
                .quantity(productToOrder.getQuantity())
                .build();

        order.setTotalAmount(order.getTotalAmount() + newOrderDetails.getTotalPrice());

        order = orderRepository.save(order);
        orderDetailsRepository.save(newOrderDetails);
        order.setOrderDetails(orderDetailsRepository.findAllByOrderId(order.getId()));

        return order;
    }

    public Order updateProduct(Long id, String userEmail, UpdateProductDTO updateProduct) {

        Order userOrderById = getUserOrderById(id, userEmail);

        OrderDetails existsOrder = userOrderById.getOrderDetails()
                .stream().filter(orderDetails -> orderDetails.getProductName().equals(updateProduct.getName()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(ApplicationExceptionHandler.PRODUCT_NOT_FOUND,
                        String.format("No product with name [%s] in order with id [%d] of user [%s]",
                                updateProduct.getName(), id, userEmail)));

        existsOrder.setQuantity(updateProduct.getQuantity());
        userOrderById.setTotalAmount(userOrderById.getTotalAmount());

        return orderRepository.save(userOrderById);
    }

    public Order deleteOrder(Long id, String userEmail) {
        Order currentOrder = getUserOrderById(id, userEmail);

        orderRepository.delete(currentOrder);

        return currentOrder;
    }

    public Order deleteProductInOrder(Long orderId, String userEmail, DeleteProductDTO deleteProduct) {
        Order order = getUserOrderById(orderId, userEmail);

        OrderDetails currentDetails = order.getOrderDetails()
                .stream()
                .filter(orderDetails -> orderDetails.getProduct().getName().equals(deleteProduct.getName()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(ApplicationExceptionHandler.PRODUCT_NOT_FOUND,
                        String.format("No product with name [%s] in order with id [%d] of user [%s]",
                                deleteProduct.getName(), orderId, userEmail)));


        order.getOrderDetails().remove(currentDetails);
        orderDetailsRepository.delete(currentDetails);

        order.setTotalAmount(order.getTotalAmount());

        return orderRepository.save(order);
    }

    public PaymentStatus payForOrder(CreditCard card, Order order) {

        ordersAmountNotGreaterThanProducts(order);

        PaymentStatus paymentStatus = new StripePaymentStatus(paymentService.pay(card, order));

        List<Bonus> bonuses = updateProductsInOrder(order);

        if (!bonuses.isEmpty()) {
            User customer = userService.getUserByEmail(order.getCustomer().getEmail());
            int sum = bonuses.stream().mapToInt(Bonus::getAmount).sum();
            customer.setTotalBonusAmount(customer.getTotalBonusAmount() + sum);
            userService.updateUser(customer);
        }

        order.setStatus(Order.Status.BOUGHT);
        order.setDate(new Timestamp(new Date().getTime()));
        orderRepository.save(order);

        Map<String, Object> payload = new HashMap<>();
        payload.put("order", order);
        payload.put("paymentStatus", paymentStatus);

        try {
            this.emailService.send(order.getCustomer(), paymentStatus.isSucceeded() ?
                    MAIL_TEMPLATE_ORDER_IS_PAYED : MAIL_TEMPLATE_ORDER_IS_NOT_PAYED, payload);
        } catch (Exception e) {
            logger.error(String.format("Unable to send email to %s about %s with payment status %s",
                    order.getCustomer().getEmail(), order, paymentStatus));
        }

        return paymentStatus;
    }

    private void ordersAmountNotGreaterThanProducts(Order order) {
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
    }

    private List<Bonus> updateProductsInOrder(Order order) {
        List<OrderDetails> orderDetails = order.getOrderDetails();
        List<Bonus> bonuses = new ArrayList<>();

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
            if (productInMarket.getBonus() != null)
                bonuses.add(productInMarket.getBonus());
            productUpdateQuantity.setCategory(productInMarket.getCategory().getName());
            productUpdateQuantity.setQuantity(productInMarket.getQuantity() - details.getQuantity());


            productService.update(productInMarket.getId(), productUpdateQuantity, productInMarket.getSeller().getEmail());
        }

        return bonuses;
    }

    public List<SellerStatistic> getCustomersWhichOrderedSellerProduct(String sellerEmail) {
        User seller = userService.getUserByEmail(sellerEmail);
        List<Product> sellerProducts = seller.getProducts();
        Map<Product, List<User>> productCustomers = new HashMap<>();

        for (Product product : sellerProducts) {
            List<User> allCustomersBySellerIdAAndProductId = orderRepository.
                    findAllCustomersBySellerIdAAndProductId(seller.getId(), product.getId());
            if (!allCustomersBySellerIdAAndProductId.isEmpty()) {
                productCustomers.put(product, allCustomersBySellerIdAAndProductId);
            }
        }

        List<SellerStatistic> sellerStatistics = new ArrayList<>();

        for (Map.Entry<Product, List<User>> entry : productCustomers.entrySet()) {
            var product = entry.getKey();
            var customers = entry.getValue();
            ProductDTO productDTO = ProductDTO.builder()
                    .name(product.getName())
                    .quantity(product.getQuantity())
                    .price(product.getPrice())
                    .category(product.getCategory().getName())
                    .bonus(product.getBonus() == null ? null : product.getBonus().getName())
                    .discount(product.getDiscount() == null ? null : product.getDiscount().getName())
                    .build();

            List<SellerStatistic.CustomerStat> customerDTOS = customers.stream()
                    .map(customer -> new SellerStatistic.CustomerStat(customer.getId(), customer.getFullName(),
                            customer.getEmail(), customer.getRole(), customer.getTotalBonusAmount(),
                            extractOrderStatusFromCustomer(customer, product)))
                    .toList();

            SellerStatistic sellerStatistic = new SellerStatistic(productDTO, customerDTOS);
            sellerStatistics.add(sellerStatistic);
        }

        return sellerStatistics;
    }

    private Order.Status extractOrderStatusFromCustomer(User customer, Product product) {
        List<Order> orders = customer.getOrders();
        for (Order order : orders) {
            List<OrderDetails> details = order.getOrderDetails();
            for (OrderDetails detail : details) {
                if (detail.getProduct().equals(product)) {
                    return order.getStatus();
                }
            }
        }

        return null;
    }
}