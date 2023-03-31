package com.example.app.controller;

import com.example.app.model.Product;
import com.example.app.service.ProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  @GetMapping()
  @PreAuthorize("hasAnyAuthority('CUSTOMER', 'SELLER', 'ADMIN')")
  ResponseEntity<List<Product>> getProduct(@RequestParam(required = false, name = "c") String category,
      @RequestParam(required = false, defaultValue = "0", name = "min") int minCost,
      @RequestParam(required = false, defaultValue = "1000000", name = "max") int maxCost) {

    List<Product> products;

    if (category == null) {
      products = productService.findAllByPriceBetween(minCost, maxCost);
    } else {
      products = productService.findAllByCategoryAndPriceBetween(category, minCost, maxCost);
    }

    return ResponseEntity.ok(products);
  }

}
