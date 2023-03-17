package com.example.app.service;

import com.example.app.exception.ApplicationExceptionHandler;
import com.example.app.exception.NotFoundException;
import com.example.app.model.Discount;
import com.example.app.repository.DiscountRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DiscountService {

  private final DiscountRepository discountRepository;

  @Autowired
  public DiscountService(DiscountRepository discountRepository) {
    this.discountRepository = discountRepository;
  }

  Discount findDiscountByName(String discountName) {
    Optional<Discount> discount = discountRepository.findByName(discountName);

    return discount.orElseThrow(
        () -> new NotFoundException(ApplicationExceptionHandler.DISCOUNT_NOT_FOUND,
            String.format("discount with name: '%s' not found", discountName)));
  }
}
