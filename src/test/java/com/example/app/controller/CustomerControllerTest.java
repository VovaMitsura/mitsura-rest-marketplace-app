package com.example.app.controller;

import com.example.app.model.User;
import com.example.app.model.User.Role;
import com.example.app.security.JwtAuthenticationFilter;
import com.example.app.utils.TokenUtil;
import java.util.HashMap;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(OrderAnnotation.class)
@Sql(scripts = {"/testSql/schema.sql", "/testSql/data.sql"})
@Sql(scripts = "/testSql/delete.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
@SpringBootTest()
class CustomerControllerTest {

  private final String CUSTOMER_URL = "/customers";

  @Autowired
  private WebApplicationContext webAppContext;

  private MockMvc mockMvc;

  @Autowired
  private JwtAuthenticationFilter authenticationFilter;

  private String jwtToken;


  @BeforeEach
  public void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext)
        .addFilter(authenticationFilter).
        build();

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
  @Order(2)
  void getAllCustomersShouldReturnStatusOk() throws Exception {
    this.mockMvc.perform(MockMvcRequestBuilders.get(CUSTOMER_URL)
            .accept(MediaType.APPLICATION_JSON)
            .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andDo(MockMvcResultHandlers.print());
  }

  @Test
  @Order(1)
  void getCustomerByIdShouldReturnStatusOk() throws Exception {
    this.mockMvc.perform(MockMvcRequestBuilders.get(CUSTOMER_URL + "/1")
            .accept(MediaType.APPLICATION_JSON)
            .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andDo(MockMvcResultHandlers.print());
  }
}