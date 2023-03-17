package com.example.app.controller;

import com.example.app.RestMarketPlaceAppApplication;
import com.example.app.controller.dto.ProductDto;
import com.example.app.exception.ApplicationExceptionHandler;
import com.example.app.exception.ApplicationExceptionHandler.ErrorResponse;
import com.example.app.model.Product;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RestMarketPlaceAppApplication.class)
@TestMethodOrder(OrderAnnotation.class)
@Sql(scripts = {"/testSql/schema.sql", "/testSql/data.sql"})
@Sql(scripts = "/testSql/delete.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
@SpringBootTest
class SellerControllerTest {

  private final String SELLER_URL = "/seller";
  @Autowired
  private WebApplicationContext webAppContext;
  @Autowired
  private ObjectMapper objectMapper;

  private MockMvc mockMvc;
  private ProductDto postProduct;
  private Product responseProduct;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();

    postProduct = new ProductDto();
    postProduct.setName("Samsung a71");
    postProduct.setPrice(18000);
    postProduct.setQuantity(40);

    responseProduct = new Product();
    responseProduct.setId(2L);
    responseProduct.setName(postProduct.getName());
    responseProduct.setPrice(postProduct.getPrice());
    responseProduct.setQuantity(postProduct.getQuantity());
  }

  @Test
  @Order(1)
  void postValidProductShouldReturnCreated() throws Exception {

    postProduct.setCategory("smartphone");

    MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(SELLER_URL + "/product")
            .content(objectMapper.writeValueAsString(postProduct))
            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isCreated()).andExpect(
            MockMvcResultMatchers.content()
                .string(objectMapper.writeValueAsString(responseProduct))).andReturn();

    JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());

    Product response = new Product(node.get("id").asLong(), node.get("name").asText(),
        node.get("price").asInt(), null, null, null,
        node.get("quantity").asInt(), null);

    Assertions.assertEquals(responseProduct.getId(), response.getId());
    Assertions.assertEquals(responseProduct.getName(), response.getName());
    Assertions.assertEquals(responseProduct.getCategory(), response.getCategory());
    Assertions.assertEquals(responseProduct.getPrice(), response.getPrice());
    Assertions.assertEquals(responseProduct.getCategory(), response.getCategory());
  }

  @Test
  @Order(2)
  void postProductWithInvalidDiscountShouldReturnNotFound() throws Exception {
    postProduct.setDiscount("lol");
    ErrorResponse response = new ErrorResponse(ApplicationExceptionHandler.DISCOUNT_NOT_FOUND,
        String.format("discount with name: '%s' not found", postProduct.getDiscount()));

    mockMvc.perform(MockMvcRequestBuilders.post(SELLER_URL + "/product")
            .content(objectMapper.writeValueAsString(postProduct))
            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isNotFound()).andExpect(
            MockMvcResultMatchers.content().string(objectMapper.writeValueAsString(response)));
  }

  @Test
  @Order(3)
  void postProductWithInvalidCategoryShouldReturnNotFound() throws Exception {
    postProduct.setCategory("lol");
    ErrorResponse response = new ErrorResponse(ApplicationExceptionHandler.CATEGORY_NOT_FOUND,
        String.format("category with name: %s not found", postProduct.getCategory()));

    mockMvc.perform(
            MockMvcRequestBuilders.post(SELLER_URL + "/product").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postProduct))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isNotFound()).andExpect(
            MockMvcResultMatchers.content().string(objectMapper.writeValueAsString(response)));
  }

  @Test
  @Order(4)
  void getAllCustomersShouldReturnOneValue() throws Exception {
    this.mockMvc.perform(MockMvcRequestBuilders.get(SELLER_URL))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andDo(MockMvcResultHandlers.print());
  }

  @Test
  @Order(5)
  void getCustomerByIdShouldReturnOneValues() throws Exception {
    this.mockMvc.perform(MockMvcRequestBuilders.get(SELLER_URL + "/4"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andDo(MockMvcResultHandlers.print());
  }

  @Test
  @Order(6)
  @Sql(scripts = "/testSql/delete.sql")
  void getAllSellersWithNoValueInDBShouldReturnNotFound() throws Exception {
    this.mockMvc.perform(MockMvcRequestBuilders.get(SELLER_URL))
        .andExpect(MockMvcResultMatchers.status().isNotFound())
        .andDo(MockMvcResultHandlers.print());
  }

  @Test
  @Order(7)
  @Sql(scripts = "/testSql/delete.sql")
  void getSellerByIdWithNoValueInDBShouldReturnNotFound() throws Exception {
    this.mockMvc.perform(MockMvcRequestBuilders.get(SELLER_URL + "/4"))
        .andExpect(MockMvcResultMatchers.status().isNotFound())
        .andDo(MockMvcResultHandlers.print());
  }
}