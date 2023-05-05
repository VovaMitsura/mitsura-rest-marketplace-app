package com.example.app.repository;

import com.example.app.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

  Optional<Order> findByIdAndCustomerEmailAndStatus(Long id, String userEmail, Order.Status status);
  List<Order> findAllByCustomerEmailAndStatus( String userEmail, Order.Status status);

}
