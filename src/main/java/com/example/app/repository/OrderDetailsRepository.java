package com.example.app.repository;

import com.example.app.model.OrderDetails;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDetailsRepository extends JpaRepository<OrderDetails, Long> {

  List<OrderDetails> findAllByOrderId(Long orderId);

}
