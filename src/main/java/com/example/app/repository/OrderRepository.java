package com.example.app.repository;

import com.example.app.model.Order;
import com.example.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdAndCustomerEmailAndStatus(Long id, String userEmail, Order.Status status);

    List<Order> findAllByCustomerEmailAndStatus(String userEmail, Order.Status status);

    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN Order o ON o.customer.id = u.id " +
            "JOIN OrderDetails od ON od.order.id = o.id " +
            "JOIN Product p ON p.id = od.product.id " +
            "WHERE p.id = :productId and p.seller.id = :sellerId AND u.role = 'CUSTOMER'")
    List<User> findAllCustomersBySellerIdAAndProductId(Long sellerId, Long productId);

}
