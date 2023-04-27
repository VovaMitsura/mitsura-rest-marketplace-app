package com.example.app.service;

import com.example.app.controller.dto.DiscountDTO;
import com.example.app.exception.ApplicationExceptionHandler;
import com.example.app.exception.NotFoundException;
import com.example.app.exception.ResourceConflictException;
import com.example.app.model.Discount;
import com.example.app.model.Product;
import com.example.app.repository.DiscountRepository;
import com.example.app.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    @MockBean
    DiscountRepository discountRepository;

    ObjectMapper mapper = new ObjectMapper();
    private final int minPrice = 0;
    private final int maxPrice = 1000000;
    List<Product> products;
    String sellerEmail;

    @BeforeEach()
    void setUp() throws Exception {
        Product[] productsArray = mapper.readValue(new File("src/test/resources/data/products.json"),
                Product[].class);
        products = Arrays.asList(productsArray);
        sellerEmail = "john@mail.com";

    }

    @Test
    void getProductWithOutCategoryShouldReturnAllValues() throws IOException {

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

    @Test
    void getProductByIdShouldReturnOneValue() throws IOException {

        Product actualProduct = products.get(0);

        Mockito.when(productRepository.findById(1L))
                .thenReturn(Optional.of(actualProduct));

        Product productById = productService.getProductById((1L));

        Assertions.assertNotNull(productById);
        Assertions.assertEquals(actualProduct.getId(), productById.getId());
        Assertions.assertEquals(actualProduct.getName(), productById.getName());
        Assertions.assertEquals(actualProduct.getQuantity(), productById.getQuantity());
        Assertions.assertEquals(actualProduct.getPrice(), productById.getPrice());
    }

    @Test
    void getNonExistingProductShouldReturnNotFound() {
        Mockito.when(productRepository.findById(10L))
                .thenReturn(Optional.empty());

        NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> productService.getProductById(10L));

        Assertions.assertNotNull(exception);
        Assertions.assertEquals(ApplicationExceptionHandler.PRODUCT_NOT_FOUND, exception.getErrorCode());
        Assertions.assertEquals("Product with id [10] not found", exception.getMessage());
    }

    @Test
    void addDiscountToProduct() throws Exception {

        Product product = products.get(0);
        DiscountDTO discountDTO = new DiscountDTO("Happy New Year disc.", 30);
        Discount discountToAdd = new Discount(1L, discountDTO.getName(), discountDTO.getPercentage(), null);

        Mockito.when(productRepository.findByIdAndSellerEmail(1L, sellerEmail))
                .thenReturn(Optional.of(product));
        Mockito.when(discountRepository.findByName(discountDTO.getName()))
                .thenReturn(Optional.empty());
        Mockito.when(discountRepository.save(Mockito.any(Discount.class)))
                .thenReturn(discountToAdd);
        Mockito.when(productRepository.save(Mockito.any()))
                .thenReturn(product);

        Product response = productService.addDiscountToProduct(1L, sellerEmail, discountDTO);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(response.getDiscount().getName(), discountDTO.getName());
        Assertions.assertEquals(response.getDiscount().getDiscountPercent(), discountDTO.getPercentage());
    }

    @Test
    void addAddExistDiscountToProductThrowException() throws Exception {
        Product product = products.get(0);
        DiscountDTO discountDTO = new DiscountDTO("Happy New Year disc.", 30);
        Discount discount = new Discount(1L, discountDTO.getName(), discountDTO.getPercentage(), null);
        product.setDiscount(discount);

        Mockito.when(productRepository.findByIdAndSellerEmail(1L, sellerEmail))
                .thenReturn(Optional.of(product));

        ResourceConflictException exception = Assertions.assertThrows(ResourceConflictException.class, () -> {
            productService.addDiscountToProduct(1L, sellerEmail, discountDTO);
        });

        Assertions.assertEquals(ApplicationExceptionHandler.DUPLICATE_ENTRY, exception.getErrorCode());
        Assertions.assertEquals(exception.getMessage(), String.format("Discount with name [%s] for product [%s] already exists",
                product.getName(), discountDTO.getName()));
    }

    @Test
    void getProductOfSellerWhichHaveNoProductThrowException() {

        Mockito.when(productRepository.findByIdAndSellerEmail(1L, sellerEmail))
                .thenReturn(Optional.empty());

        NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () -> {
            productService.getProductByIdAndSellerEmail(1L, sellerEmail);
        });

        Assertions.assertEquals(ApplicationExceptionHandler.PRODUCT_NOT_FOUND, exception.getErrorCode());
        Assertions.assertEquals(exception.getMessage(),  String.format("User with email [%s] has no product with id [%d]",
                sellerEmail, 1L));
    }
}