package com.example.app.repository;

import com.example.app.model.Order;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

  List<Order> findAllByCustomerEmail(String userEmail);
  Optional<Order> findByIdAndCustomerEmail(Long id, String userEmail);

}
