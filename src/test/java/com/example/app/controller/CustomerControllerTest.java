package com.example.app.controller;

import com.example.app.exception.ApplicationExceptionHandler;
import com.example.app.model.User;
import com.example.app.repository.OrderRepository;
import com.example.app.security.JwtAuthenticationFilter;
import com.example.app.utils.TokenUtil;
import com.example.app.utils.factory.CustomerFactory;
import com.example.app.utils.factory.UserFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
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

import java.util.List;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(OrderAnnotation.class)
@Sql(scripts = {"/testSql/schema.sql", "/testSql/data.sql"})
@Sql(scripts = "/testSql/delete.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
@SpringBootTest()
class CustomerControllerTest {

    private final String CUSTOMER_URL = "/api/v1/customers";

    @Autowired
    private WebApplicationContext webAppContext;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private JwtAuthenticationFilter authenticationFilter;
    @Autowired
    private ObjectMapper mapper;

    private MockMvc mockMvc;
    private String jwtToken;
    private User user;


    @BeforeEach
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext)
                .addFilter(authenticationFilter).
                build();

        UserFactory factory = new CustomerFactory();
        user = factory.createUser();

        jwtToken = TokenUtil.createToken(user);
    }

    @Test
    @Order(1)
    void getCustomerByIdShouldReturnStatusOk() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get(CUSTOMER_URL + "/1")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        User response = mapper.readValue(mvcResult.getResponse().getContentAsString(), User.class);

        Assertions.assertEquals(1L, response.getId());
        Assertions.assertEquals("John Smith", response.getFullName());
        Assertions.assertEquals(User.Role.CUSTOMER, response.getRole());
        Assertions.assertEquals(1, response.getOrders().size());
    }

    @Test
    @Order(2)
    void getCustomerByInvalidIdShouldThrowException() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get(CUSTOMER_URL + "/111111")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        ApplicationExceptionHandler.ErrorResponse exception = mapper.readValue(mvcResult.getResponse().getContentAsString(),
                ApplicationExceptionHandler.ErrorResponse.class);

        Assertions.assertEquals(ApplicationExceptionHandler.USER_NOT_FOUND, exception.getErrorCode());
        Assertions.assertEquals(String.format("No exists %s with id: %d in system", User.Role.CUSTOMER, 111111),
                exception.getErrorMessage());
    }

    @Test
    @Order(3)
    void getCustomerHistoryShouldReturnList() throws Exception {
        var userOrders = orderRepository.findAllByCustomerEmailAndStatus(user.getEmail(),
                com.example.app.model.Order.Status.CREATED);
        var createdOrder = userOrders.get(0);
        createdOrder.setStatus(com.example.app.model.Order.Status.BOUGHT);
        orderRepository.save(createdOrder);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(CUSTOMER_URL + "/history")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        var response = List.of(mapper.readValue(mvcResult.getResponse().getContentAsString(),
                com.example.app.model.Order[].class));

        Assertions.assertEquals(1, response.size());
        Assertions.assertEquals(com.example.app.model.Order.Status.BOUGHT, response.get(0).getStatus());
    }

    @Test
    @Order(4)
    void getCustomerHistoryShouldThrowException() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(CUSTOMER_URL + "/history")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();

        ApplicationExceptionHandler.ErrorResponse response = mapper.readValue(mvcResult.getResponse().getContentAsString(),
                ApplicationExceptionHandler.ErrorResponse.class);

        Assertions.assertEquals(ApplicationExceptionHandler.ORDER_NOT_FOUND, response.getErrorCode());
        Assertions.assertEquals(String.format("No order of user [%s]", user.getEmail()), response.getErrorMessage());
    }
}
