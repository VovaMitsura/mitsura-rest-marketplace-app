package com.example.app.controller;

import com.example.app.RestMarketPlaceAppApplication;
import com.example.app.exception.ApplicationExceptionHandler;
import com.example.app.exception.ApplicationExceptionHandler.ErrorResponse;
import com.example.app.model.Product;
import com.example.app.model.User;
import com.example.app.repository.ProductRepository;
import com.example.app.utils.TokenUtil;
import com.example.app.utils.factory.AdminFactory;
import com.example.app.utils.factory.UserFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

@ExtendWith(SpringExtension.class)
@Sql(scripts = {"/testSql/schema.sql", "/testSql/data.sql"})
@Sql(scripts = "/testSql/delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SpringBootTest(classes = RestMarketPlaceAppApplication.class)
class ProductControllerTest {
    private static final String URL = "/api/v1/product";

    @Autowired
    WebApplicationContext webAppContext;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    ProductRepository productRepository;

    MockMvc mockMvc;
    String jwtToken;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        UserFactory factory = new AdminFactory();
        User user =  factory.createUser();

        jwtToken = TokenUtil.createToken(user);
    }

    @Test
    void getAllProductsWithPriceFiltersShouldReturnList() throws Exception {
        List<Product> allByPriceBetween = productRepository.findAllByPriceBetween(8500, 18000);

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get(URL + "?min=8000&max=18000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        List<Product> response = List.of(mapper.readValue(mvcResult.getResponse().getContentAsString(),
                Product[].class));

        Assertions.assertNotNull(response);
        Assertions.assertEquals(response.size(), allByPriceBetween.size());
        Assertions.assertEquals(response.get(0).getName(), allByPriceBetween.get(0).getName());
        Assertions.assertEquals(response.get(1).getPrice(), allByPriceBetween.get(1).getPrice());
        Assertions.assertEquals(response.get(0).getQuantity(), allByPriceBetween.get(0).getQuantity());
    }

    @Test
    void getAllProductsWithCategoryFiltersShouldReturnList() throws Exception {
        List<Product> allByCategory = productRepository.findAllByCategoryNameAndPriceBetween("smartphone",
                0, 1000000);

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get(URL + "?c=smartphone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        List<Product> response = List.of(mapper.readValue(mvcResult.getResponse().getContentAsString(),
                Product[].class));

        Assertions.assertNotNull(response);
        Assertions.assertEquals(response.size(), allByCategory.size());
        Assertions.assertEquals(response.get(0).getName(), allByCategory.get(0).getName());
        Assertions.assertEquals(response.get(1).getPrice(), allByCategory.get(1).getPrice());
        Assertions.assertEquals(response.get(0).getQuantity(), allByCategory.get(0).getQuantity());
    }

    @Test
    void getProductByIdShouldReturnStatusOk() throws Exception {
        Product product = productRepository.findById(1L).get();

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get(URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        Product response = mapper.readValue(mvcResult.getResponse().getContentAsString(), Product.class);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(response.getName(), product.getName());
    }

    @Test
    void findProductNonExistingProductShouldReturnError() throws Exception {
        ErrorResponse errorResponse = new ErrorResponse(ApplicationExceptionHandler.PRODUCT_NOT_FOUND,
                "Product with id [10] not found");

        this.mockMvc.perform(MockMvcRequestBuilders.get(URL + "/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string(mapper.writeValueAsString(errorResponse)));
    }
}