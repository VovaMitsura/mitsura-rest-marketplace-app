package com.example.app.controller;

import com.example.app.RestMarketPlaceAppApplication;
import com.example.app.controller.dto.AuthenticationRequest;
import com.example.app.controller.dto.RegisterRequest;
import com.example.app.exception.ApplicationExceptionHandler;
import com.example.app.exception.ApplicationExceptionHandler.ErrorResponse;
import com.example.app.model.User;
import com.example.app.model.User.Role;
import com.example.app.repository.UserRepository;
import com.example.app.security.SimpleUserPrinciple;
import com.example.app.utils.TokenUtil;
import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {RestMarketPlaceAppApplication.class})
@Sql(scripts = {"/testSql/schema.sql", "/testSql/data.sql"})
@Sql(scripts = "/testSql/delete.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class AuthenticationControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserRepository userRepository;

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

    @Test
    void getCurrentUserShouldReturnStatusOk() throws Exception {

        User currentUser = userRepository.findByEmail("john@mail.com").orElseThrow();

        var roles = new HashMap<String, Object>();
        roles.put("role", currentUser.getRole());
        String jwtToken = TokenUtil.createToken(roles, currentUser.getEmail());

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        JsonNode jsonNode = mapper.readTree(mvcResult.getResponse().getContentAsString());
        User userResponse = mapper.convertValue(jsonNode.get("user"), User.class);
        SimpleUserPrinciple response = new SimpleUserPrinciple(userResponse);

        Assertions.assertEquals(response.getUser().getId(), currentUser.getId());
        Assertions.assertEquals(response.getUser().getRole(), currentUser.getRole());
        Assertions.assertEquals(response.getUsername(), currentUser.getEmail());
        Assertions.assertEquals(response.getAuthorities().toArray()[0].toString(), currentUser.getRole().toString());
        Assertions.assertTrue(response.isAccountNonExpired());
        Assertions.assertTrue(response.isAccountNonLocked());
        Assertions.assertTrue(response.isCredentialsNonExpired());
        Assertions.assertTrue(response.isEnabled());
    }

    @Test
    void addUserWithInvalidInputShouldThrowException() throws Exception {
        registerRequest.setEmail("");

        ErrorResponse errorResponse = new ErrorResponse("invalid_input", "Invalid request content.");

        this.mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(registerRequest)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string(mapper.writeValueAsString(errorResponse)));
    }
}