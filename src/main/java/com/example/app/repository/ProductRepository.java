package com.example.app.repository;

import com.example.app.model.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  Optional<Product> findProductByNameAndSellerFullName(String productName, String sellerFullName);
  List<Product> findAllByPriceBetween(int price, int price2);

  List<Product> findAllByCategoryNameAndPriceBetween(String category, int price, int price2);
}

