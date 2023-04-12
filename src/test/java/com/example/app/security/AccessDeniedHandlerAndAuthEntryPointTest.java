package com.example.app.security;

import com.example.app.RestMarketPlaceAppApplication;
import com.example.app.exception.ApplicationExceptionHandler;
import com.example.app.exception.ApplicationExceptionHandler.ErrorResponse;
import com.example.app.model.User;
import com.example.app.repository.ProductRepository;
import com.example.app.service.ProductService;
import com.example.app.service.UserService;
import com.example.app.utils.TokenUtil;
import com.example.app.utils.factory.CustomerFactory;
import com.example.app.utils.factory.UserFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {RestMarketPlaceAppApplication.class})
@Sql(scripts = {"/testSql/schema.sql", "/testSql/data.sql"})
@Sql(scripts = "/testSql/delete.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class AccessDeniedHandlerAndAuthEntryPointTest {

  @Autowired
  private WebApplicationContext webApplicationContext;
  @Autowired
  private JwtAuthenticationFilter authenticationFilter;
  @Autowired
  private ObjectMapper mapper;

  @MockBean
  ProductService productService;
  @MockBean
  ProductRepository productRepository;
  @MockBean
  UserService userService;

  private MockMvc mockMvc;

  private static final String URL = "/api/v1/seller";

  private String jwtToken;

  @BeforeEach
  void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
        .apply(SecurityMockMvcConfigurers.springSecurity())
        .addFilter(authenticationFilter)
        .build();

    UserFactory factory = new CustomerFactory();
    User user = factory.createUser();

    jwtToken = TokenUtil.createToken(user);
  }

  @Test
  void getAllSellersWithCustomerRoleShouldReturnForbidden() throws Exception {
    ErrorResponse errorResponse = new ErrorResponse(ApplicationExceptionHandler.NO_PERMISSION,
        "You are not authorize to use this functionality");

    this.mockMvc.perform(MockMvcRequestBuilders.get(URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
        .andExpect(MockMvcResultMatchers.status().isForbidden())
        .andExpect(
            MockMvcResultMatchers.content().string(mapper.writeValueAsString(errorResponse)));
  }

  @Test
  void getAllSellerWithoutAnyCredentialsShouldReturnUnauthorized() throws Exception {
    ErrorResponse errorResponse = new ErrorResponse(ApplicationExceptionHandler.NO_PERMISSION,
        "Have not provide any credentials");

    this.mockMvc.perform(MockMvcRequestBuilders.get(URL)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isUnauthorized())
        .andExpect(
            MockMvcResultMatchers.content().string(mapper.writeValueAsString(errorResponse)));
  }

}