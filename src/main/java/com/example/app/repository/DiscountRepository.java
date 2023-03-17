package com.example.app.repository;

import com.example.app.model.Discount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscountRepository extends JpaRepository<Discount, Long> {

  Optional<Discount> findByName(String name);
}
