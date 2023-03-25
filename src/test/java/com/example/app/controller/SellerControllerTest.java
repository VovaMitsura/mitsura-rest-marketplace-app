package com.example.app.controller;

import com.example.app.RestMarketPlaceAppApplication;
import com.example.app.controller.dto.ProductDto;
import com.example.app.exception.ApplicationExceptionHandler;
import com.example.app.exception.ApplicationExceptionHandler.ErrorResponse;
import com.example.app.model.Product;
import com.example.app.model.User;
import com.example.app.model.User.Role;
import com.example.app.security.JwtAuthenticationFilter;
import com.example.app.utils.TokenUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
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
@TestMethodOrder(OrderAnnotation.class)
@Sql(scripts = {"/testSql/schema.sql", "/testSql/data.sql"})
@Sql(scripts = "/testSql/delete.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
@SpringBootTest(classes = RestMarketPlaceAppApplication.class)
class SellerControllerTest {

  private final String SELLER_URL = "/api/v1/seller";
  @Autowired
  private WebApplicationContext webAppContext;
  @Autowired
  private ObjectMapper objectMapper;

  private MockMvc mockMvc;

  @Autowired
  private JwtAuthenticationFilter authenticationFilter;

  private String jwtToken;

  private ProductDto postProduct;

  private Product responseProduct;

  @BeforeEach
  void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext)
        .addFilter(authenticationFilter).
        build();

    postProduct = new ProductDto();
    postProduct.setName("Samsung a71");
    postProduct.setPrice(18000);
    postProduct.setQuantity(40);

    responseProduct = new Product();
    responseProduct.setId(2L);
    responseProduct.setName(postProduct.getName());
    responseProduct.setPrice(postProduct.getPrice());
    responseProduct.setQuantity(postProduct.getQuantity());

    User user = User.builder().id(1L)
        .fullName("John Smith")
        .role(Role.CUSTOMER)
        .email("john@mail.com")
        .password("123456")
        .build();

    var roles = new HashMap<String, Object>();
    roles.put("role", user.getRole());
    jwtToken = TokenUtil.createToken(roles, user.getEmail());
  }

  @Test
  @Order(1)
  void postValidProductShouldReturnCreated() throws Exception {

    postProduct.setCategory("smartphone");

    MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(SELLER_URL + "/product")
            .content(objectMapper.writeValueAsString(postProduct))
            .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken)
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
            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
            .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
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
                .accept(MediaType.APPLICATION_JSON)
                .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
        .andExpect(MockMvcResultMatchers.status().isNotFound()).andExpect(
            MockMvcResultMatchers.content().string(objectMapper.writeValueAsString(response)));
  }

  @Test
  @Order(4)
  void getAllCustomersShouldReturnOneValue() throws Exception {
    this.mockMvc.perform(MockMvcRequestBuilders.get(SELLER_URL)
            .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andDo(MockMvcResultHandlers.print());
  }

  @Test
  @Order(5)
  void getCustomerByIdShouldReturnOneValues() throws Exception {
    this.mockMvc.perform(MockMvcRequestBuilders.get(SELLER_URL + "/4")
            .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andDo(MockMvcResultHandlers.print());
  }

}
