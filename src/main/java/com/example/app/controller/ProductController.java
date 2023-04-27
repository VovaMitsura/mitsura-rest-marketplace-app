package com.example.app.controller;

import com.example.app.model.Product;
import com.example.app.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@PreAuthorize("hasAnyAuthority('CUSTOMER', 'SELLER', 'ADMIN')")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping()
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

    @GetMapping("/{id}")
    ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

}
