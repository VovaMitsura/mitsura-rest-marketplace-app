package com.example.app.service;

import com.example.app.exception.ApplicationExceptionHandler;
import com.example.app.exception.NotFoundException;
import com.example.app.model.Product;
import com.example.app.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ProductService.class)
class ProductServiceTest {

  @Autowired
  ProductService productService;

  @MockBean
  ProductRepository productRepository;
  @MockBean
  CategoryService categoryService;
  @MockBean
  DiscountService discountService;

  ObjectMapper mapper = new ObjectMapper();

  private final int minPrice = 0;
  private final int maxPrice = 1000000;

  List<Product> products;

  @Test
  void getProductWithOutCategoryShouldReturnAllValues() throws IOException {

    Product[] productsArray = mapper.readValue(new File("src/test/resources/data/products.json"),
        Product[].class);
    products = Arrays.asList(productsArray);

    Mockito.when(productRepository.findAllByPriceBetween(minPrice, maxPrice)).thenReturn(products);

    List<Product> allProducts = productService.findAllByPriceBetween(minPrice, maxPrice);

    Assertions.assertEquals(allProducts.size(), products.size());
    Assertions.assertEquals("Samsung m53", allProducts.get(0).getName());
    Assertions.assertEquals(22000, allProducts.get(1).getPrice());
    Assertions.assertEquals(30, allProducts.get(2).getQuantity());
    Assertions.assertEquals(4L, allProducts.get(3).getId());
  }

  @Test
  void getProductWithInvalidPriceShouldReturnError() {

    Mockito.when(productRepository.findAllByPriceBetween(0, 0)).thenReturn(Collections.emptyList());

    NotFoundException notFound = Assertions.assertThrows(NotFoundException.class, () -> productService.findAllByPriceBetween(0, 0), String.format("No product with price between [%d] and [%d] in store", 0, 0));

    Assertions.assertEquals(ApplicationExceptionHandler.PRODUCT_NOT_FOUND, notFound.getErrorCode());

  }

  @Test
  void getProductWithCategorySmartphoneShouldReturnThreeValue() throws IOException {

    Product[] productsArray = mapper.readValue(new File("src/test/resources/data/smartphones.json"),
        Product[].class);
    products = Arrays.asList(productsArray);

    Mockito.when(
            productRepository.findAllByCategoryNameAndPriceBetween("smartphone", minPrice, maxPrice))
        .thenReturn(products);

    List<Product> allProducts = productService.findAllByCategoryAndPriceBetween("smartphone",
        minPrice, maxPrice);

    Assertions.assertEquals(allProducts.size(), products.size());
    Assertions.assertEquals("Samsung m53", allProducts.get(0).getName());
    Assertions.assertEquals("Samsung a54", allProducts.get(1).getName());
    Assertions.assertEquals("Honor h2", allProducts.get(2).getName());
  }

  @Test
  void getAllProductWithInvalidCategoryReturnError() {
    Mockito.when(productRepository.findAllByCategoryNameAndPriceBetween("abc", 0, 0))
        .thenReturn(Collections.emptyList());

    NotFoundException notFound = Assertions.assertThrows(NotFoundException.class, () -> productService.findAllByCategoryAndPriceBetween("abc", 0, 0), String.format("No product with category [%s] and price between [%d] and [%d] in store",
        "abc", 0, 0));
    Assertions.assertEquals(ApplicationExceptionHandler.PRODUCT_NOT_FOUND, notFound.getErrorCode());

  }
}