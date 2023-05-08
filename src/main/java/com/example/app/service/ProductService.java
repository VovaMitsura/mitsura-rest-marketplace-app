package com.example.app.service;

import com.example.app.controller.dto.DiscountDTO;
import com.example.app.controller.dto.ProductDTO;
import com.example.app.exception.ApplicationExceptionHandler;
import com.example.app.exception.NotFoundException;
import com.example.app.exception.ResourceConflictException;
import com.example.app.model.*;
import com.example.app.repository.BonusRepository;
import com.example.app.repository.DiscountRepository;
import com.example.app.repository.ProductRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final DiscountRepository discountRepository;
    private final CategoryService categoryService;
    private final DiscountService discountService;
    private final BonusRepository bonusRepository;

    @Autowired
    public ProductService(ProductRepository productRepository, DiscountRepository discountRepository, CategoryService categoryService,
                          DiscountService discountService, BonusRepository bonusRepository) {
        this.productRepository = productRepository;
        this.discountRepository = discountRepository;
        this.categoryService = categoryService;
        this.discountService = discountService;
        this.bonusRepository = bonusRepository;
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

    public Product update(Long id, ProductDTO update, String userEmail) {
        Product product = getProductByIdAndSellerEmail(id, userEmail);

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
        if (Objects.nonNull(update.getBonus())) {
            Optional<Bonus> optBonus = bonusRepository.findByName(update.getBonus());
            optBonus.ifPresent(product::setBonus);
        }

        return productRepository.save(product);
    }

    public Product delete(Long id, String userEmail) {
        Product product = getProductByIdAndSellerEmail(id, userEmail);

        productRepository.delete(product);

        return product;
    }

    public Product getProductByIdAndSellerEmail(Long id, String email) {
        return productRepository.findByIdAndSellerEmail(id, email)
                .orElseThrow(() -> new NotFoundException(ApplicationExceptionHandler.PRODUCT_NOT_FOUND,
                        String.format("User with email [%s] has no product with id [%d]", email, id)));
    }

    public Product addDiscountToProduct(Long productId, String userEmail, DiscountDTO discountDTO) {
        Product currentProduct = getProductByIdAndSellerEmail(productId, userEmail);

        Discount productDisc = currentProduct.getDiscount();
        if (Objects.nonNull(productDisc) && productDisc.getName().equals(discountDTO.getName())) {
            throw new ResourceConflictException(ApplicationExceptionHandler.DUPLICATE_ENTRY,
                    String.format("Discount with name [%s] for product [%s] already exists", currentProduct.getName(),
                            discountDTO.getName()));
        }

        Optional<Discount> optionalDiscount = discountRepository.findByName(discountDTO.getName());
        Discount discount;

        if (optionalDiscount.isPresent()) {
            discount = optionalDiscount.get();
        } else {
            discount = new Discount();
            discount.setName(discountDTO.getName());
            discount.setDiscountPercent(discountDTO.getPercentage());
            discount = discountRepository.save(discount);
        }

        currentProduct.setDiscount(discount);
        currentProduct = productRepository.save(currentProduct);

        return currentProduct;
    }
}

