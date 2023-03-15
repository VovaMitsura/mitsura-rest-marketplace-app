package com.example.app.controller;

import com.example.app.RestMarketPlaceAppApplication;
import com.example.app.exception.ApplicationExceptionHandler;
import com.example.app.exception.ApplicationExceptionHandler.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RestMarketPlaceAppApplication.class)
@TestMethodOrder(OrderAnnotation.class)
@SpringBootTest()
class CustomerControllerTest {

  private final String CUSTOMER_URL = "/customers";

  @Autowired
  private WebApplicationContext webAppContext;

  @Autowired
  private ObjectMapper objectMapper;
  private MockMvc mockMvc;


  @BeforeEach
  public void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).
        build();
  }

  @Test
  @Order(2)
  @Sql(scripts = "/testSql/data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/testSql/delete.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
  void getAllCustomersShouldReturnStatusOk() throws Exception {
    this.mockMvc.perform(MockMvcRequestBuilders.get(CUSTOMER_URL)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andDo(MockMvcResultHandlers.print());
  }

  @Test
  @Order(1)
  @Sql(scripts = "/testSql/data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/testSql/delete.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
  void getCustomerByIdShouldReturnStatusOk() throws Exception {
    this.mockMvc.perform(MockMvcRequestBuilders.get(CUSTOMER_URL + "/1")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andDo(MockMvcResultHandlers.print());
  }

  @Test
  @Order(3)
  void getAllCustomersShouldReturnNotFound() throws Exception {

    ErrorResponse errorResponse = new ErrorResponse(ApplicationExceptionHandler.USER_NOT_FOUND,
        "no exists CUSTOMERs in system");

    this.mockMvc.perform(MockMvcRequestBuilders.get(CUSTOMER_URL)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isNotFound())
        .andExpect(
            MockMvcResultMatchers.content().string(objectMapper.writeValueAsString(errorResponse)))
        .andDo(MockMvcResultHandlers.print());
  }

  @Test
  @Order(4)
  void getCustomerByIdShouldReturnNotFound() throws Exception {

    ErrorResponse errorResponse = new ErrorResponse(ApplicationExceptionHandler.USER_NOT_FOUND,
        "no exists CUSTOMER with id: 1 in system");

    this.mockMvc.perform(MockMvcRequestBuilders.get(CUSTOMER_URL + "/1")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isNotFound())
        .andExpect(
            MockMvcResultMatchers.content().string(objectMapper.writeValueAsString(errorResponse)))
        .andDo(MockMvcResultHandlers.print());

  }
}