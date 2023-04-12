package com.example.app.service;

import com.example.app.controller.dto.ProductDTO;
import com.example.app.exception.ApplicationExceptionHandler;
import com.example.app.exception.NotFoundException;
import com.example.app.model.Category;
import com.example.app.model.Discount;
import com.example.app.model.Product;
import com.example.app.model.User;
import com.example.app.repository.ProductRepository;

import java.util.List;
import java.util.Objects;
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

    public Product createProduct(ProductDTO productDto, User seller) {
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

    public List<Product> findAllByPriceBetween(int minPrice, int maxPrice) {
        List<Product> products = productRepository.findAllByPriceBetween(minPrice,
                maxPrice);

        if (products.isEmpty()) {
            throw new NotFoundException(ApplicationExceptionHandler.PRODUCT_NOT_FOUND,
                    String.format("No product with price between [%d] and [%d] in store", minPrice,
                            maxPrice));
        }

        return products;
    }

    public List<Product> findAllByCategoryAndPriceBetween(String category, int minPrice,
                                                          int maxPrice) {
        List<Product> products = productRepository.findAllByCategoryNameAndPriceBetween(
                category, minPrice, maxPrice);

        if (products.isEmpty()) {
            throw new NotFoundException(ApplicationExceptionHandler.PRODUCT_NOT_FOUND,
                    String.format("No product with category [%s] and price between [%d] and [%d] in store",
                            category, minPrice, maxPrice));
        }

        return products;
    }

    public Product getProductById(Long productId) {
        Optional<Product> optionalProduct = productRepository.findById(productId);

        if (optionalProduct.isEmpty()) {
            throw new NotFoundException(ApplicationExceptionHandler.PRODUCT_NOT_FOUND,
                    String.format("Product with id [%d] not found", productId));
        }

        return optionalProduct.get();
    }

    public Product updateProduct(Long id, ProductDTO update, String userEmail) {
        Product product = productRepository.findByIdAndSellerEmail(id, userEmail)
                .orElseThrow(() -> new NotFoundException(ApplicationExceptionHandler.PRODUCT_NOT_FOUND,
                       String.format("User with email [%s] has no product with id [%d]", userEmail, id)));

        product.setName(update.getName());
        product.setQuantity(update.getQuantity());
        product.setPrice(update.getPrice());

        if (Objects.nonNull(update.getDiscount())) {
            Discount updateDiscount = discountService.findDiscountByName(update.getDiscount());
            product.setDiscount(updateDiscount);
        }
        if (Objects.nonNull(update.getCategory())) {
            Category updateCategory = categoryService.findCategoryByName(update.getCategory());
            product.setCategory(updateCategory);
        }

        return productRepository.save(product);
    }
}

