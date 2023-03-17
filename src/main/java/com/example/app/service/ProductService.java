package com.example.app.service;

import com.example.app.controller.dto.ProductDto;
import com.example.app.model.Product;
import com.example.app.model.User;
import com.example.app.repository.ProductRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

  private final ProductRepository productRepository;
  private final CategoryService categoryService;
  private final DiscountService discountService;

  @Autowired
  public ProductService(ProductRepository productRepository, CategoryService categoryService,
      DiscountService discountService) {
    this.productRepository = productRepository;
    this.categoryService = categoryService;
    this.discountService = discountService;
  }

  public Product createProduct(ProductDto productDto, User seller) {
    Product saveProduct = new Product();

    if (productDto.getDiscount() != null) {
      saveProduct.setDiscount(discountService.findDiscountByName(productDto.getDiscount()));
    }

    saveProduct.setCategory(categoryService.findCategoryByName(productDto.getCategory()));

    Optional<Product> productByNameAndSellerFullName = productRepository.findProductByNameAndSellerFullName(
        productDto.getName(), seller.getFullName());

    if (productByNameAndSellerFullName.isPresent()) {
      productByNameAndSellerFullName.ifPresent(product -> {
        saveProduct.setId(product.getId());
        saveProduct.setQuantity(productDto.getQuantity() + product.getQuantity());
        saveProduct.setOrderDetails(product.getOrderDetails());
      });
    } else {
      saveProduct.setQuantity(productDto.getQuantity());
    }

    saveProduct.setName(productDto.getName());
    saveProduct.setPrice(productDto.getPrice());
    saveProduct.setSeller(seller);

    return productRepository.save(saveProduct);
  }
}
