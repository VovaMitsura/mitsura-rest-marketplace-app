package com.example.app.controller;

import com.example.app.RestMarketPlaceAppApplication;
import com.example.app.controller.dto.AuthenticationRequest;
import com.example.app.controller.dto.RegisterRequest;
import com.example.app.exception.ApplicationExceptionHandler;
import com.example.app.exception.ApplicationExceptionHandler.ErrorResponse;
import com.example.app.model.User;
import com.example.app.model.User.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
class AuthenticationControllerTest {

  @Autowired
  private WebApplicationContext webApplicationContext;

  @Autowired
  private ObjectMapper mapper;

  private MockMvc mockMvc;

  private static final String BASE_URL = "/api/v1/auth";

  private RegisterRequest registerRequest;

  private AuthenticationRequest authenticationRequest;

  @BeforeEach
  void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
        .apply(SecurityMockMvcConfigurers.springSecurity())
        .build();

    User user = User.builder()
        .id(1L)
        .fullName("CoCa BiBsBa")
        .email("CoCa@mail.org")
        .role(Role.CUSTOMER)
        .password("CocO753")
        .build();

    registerRequest = new RegisterRequest(user.getFullName(), user.getEmail(), user.getPassword(),
        user.getRole().toString());
    authenticationRequest = new AuthenticationRequest(user.getEmail(), user.getPassword());
  }

  @Test
  void registerSameUserAccountTwoTimeShouldThrowException() throws Exception {
    this.mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(registerRequest)))
        .andExpect(MockMvcResultMatchers.status().isCreated());

    this.mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(registerRequest)))
        .andExpect(MockMvcResultMatchers.status().isConflict());
  }


  @Test
  void authenticateExistUserShouldReturnStatusOk() throws Exception {
    this.mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(registerRequest)))
        .andExpect(MockMvcResultMatchers.status().isCreated());

    this.mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/authenticate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(authenticationRequest)))
        .andExpect(MockMvcResultMatchers.status().isOk());
  }

  @Test
  void authenticateNonExistingUserShouldReturnUnauthorized() throws Exception {
    ErrorResponse errorResponse = new ErrorResponse(ApplicationExceptionHandler.NO_PERMISSION,
        "Have not provide any credentials");

    this.mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/authenticate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(authenticationRequest)))
        .andExpect(MockMvcResultMatchers.status().isUnauthorized())
        .andExpect(MockMvcResultMatchers.content().string(mapper.writeValueAsString(errorResponse)));
  }


}