package com.example.app.service;

import com.example.app.exception.ApplicationExceptionHandler;
import com.example.app.exception.NotFoundException;
import com.example.app.model.Discount;
import com.example.app.repository.DiscountRepository;
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
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = DiscountService.class)
class DiscountServiceTest {

    @Autowired
    DiscountService discountService;

    @MockBean
    DiscountRepository discountRepository;

    ObjectMapper mapper = new ObjectMapper();
    List<Discount> discounts;

    @BeforeEach
    void setUp() throws IOException {
        discounts = List.of(mapper.readValue(new File("src/test/resources/data/discounts.json"),
                Discount[].class));
    }

    @Test
    void getDiscountByNameReturnDiscount() {
        Optional<Discount> discountOptional = discounts.stream()
                .filter(discount -> discount.getName().equals("Black Friday disc."))
                .findFirst();

        Mockito.when(discountRepository.findByName("Black Friday disc."))
                .thenReturn(discountOptional);

        var response = discountService.findDiscountByName("Black Friday disc.");

        Assertions.assertNotNull(response);
        Assertions.assertEquals(discountOptional.get(), response);
        Assertions.assertEquals(discountOptional.get().getName(), response.getName());
        Assertions.assertEquals(discountOptional.get().getDiscountPercent(), response.getDiscountPercent());
    }

    @Test
    void getNonExistingDiscountThrowException() {
        Mockito.when(discountRepository.findByName("none"))
                .thenReturn(Optional.empty());

        var exception = Assertions.assertThrows(NotFoundException.class, () ->
                discountService.findDiscountByName("none"));

        Assertions.assertNotNull(exception);
        Assertions.assertEquals(ApplicationExceptionHandler.DISCOUNT_NOT_FOUND, exception.getErrorCode());
        Assertions.assertEquals(String.format("discount with name: '%s' not found", "none"),
                exception.getMessage());
    }

}