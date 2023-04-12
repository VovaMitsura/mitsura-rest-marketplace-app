package com.example.app.controller;

import com.example.app.RestMarketPlaceAppApplication;
import com.example.app.controller.dto.OrderCreationResponseDTO;
import com.example.app.controller.dto.ProductToOrderDTO;
import com.example.app.exception.ApplicationExceptionHandler.ErrorResponse;
import com.example.app.model.Order;
import com.example.app.model.User;
import com.example.app.repository.OrderRepository;
import com.example.app.utils.TokenUtil;
import com.example.app.utils.factory.CustomerFactory;
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

import java.util.HashMap;
import java.util.List;

@ExtendWith(SpringExtension.class)
@Sql(scripts = {"/testSql/schema.sql", "/testSql/data.sql"})
@Sql(scripts = "/testSql/delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SpringBootTest(classes = RestMarketPlaceAppApplication.class)
class OrderControllerTest {
    private static final String BASE_URL = "/api/v1/order";

    @Autowired
    WebApplicationContext webAppContext;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    OrderRepository orderRepository;

    MockMvc mockMvc;
    String jwtToken;
    User user;
    ProductToOrderDTO request;
    OrderCreationResponseDTO response;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        UserFactory factory = new CustomerFactory();
        user = factory.createUser();

        jwtToken = TokenUtil.createToken(user);
    }

    @Test
    void getAllCustomerOrdersShouldReturnOk() throws Exception {

        List<Order> allByCustomerOrders = orderRepository.findAllByCustomerEmail(user.getEmail());
        Order orderFromRepository = allByCustomerOrders.get(0);

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        List<Order> orders = List.of(mapper.readValue(mvcResult.getResponse().getContentAsString(), Order[].class));
        Order orderFromResponse = orders.get(0);

        Assertions.assertEquals(orderFromResponse.getId(), orderFromRepository.getId());
        Assertions.assertEquals(orderFromResponse.getStatus(), orderFromRepository.getStatus());
        Assertions.assertEquals(orderFromResponse.getOrderDetails().get(0).getQuantity(),
                orderFromRepository.getOrderDetails().get(0).getQuantity());
    }

    @Test
    void addProductToExistingOrderShouldUpdateTotalAmount() throws Exception {
        request = ProductToOrderDTO.builder()
                .orderId(1L)
                .productId(1L)
                .quantity(1)
                .build();

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
                        .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();

        response = mapper.readValue(mvcResult.getResponse().getContentAsString(), OrderCreationResponseDTO.class);

        List<Order> allByCustomerOrders = orderRepository.findAllByCustomerEmail(user.getEmail());
        OrderCreationResponseDTO createdOrder = OrderCreationResponseDTO
                .builder()
                .message("Order successfully created")
                .order(allByCustomerOrders.get(0))
                .build();


        Assertions.assertEquals(response.getMessage(), createdOrder.getMessage());
        Assertions.assertEquals(response.getOrder().getId(), createdOrder.getOrder().getId());
        Assertions.assertEquals(response.getOrder().getStatus(), createdOrder.getOrder().getStatus());
    }

    @Test
    void addProductWithInvalidBodyShouldThrowException() throws Exception {
        request = ProductToOrderDTO.builder()
                .orderId(-1L)
                .productId(1L)
                .quantity(1)
                .build();

        ErrorResponse errorResponse = new ErrorResponse("invalid_input", "Invalid request content.");

        this.mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
                        .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string(mapper.writeValueAsString(errorResponse)));

    }
}