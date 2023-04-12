package com.example.app.controller;

import com.example.app.RestMarketPlaceAppApplication;
import com.example.app.controller.dto.CategoryDTO;
import com.example.app.exception.ApplicationExceptionHandler;
import com.example.app.exception.ApplicationExceptionHandler.ErrorResponse;
import com.example.app.model.Category;
import com.example.app.model.User;
import com.example.app.model.User.Role;
import com.example.app.utils.TokenUtil;
import com.example.app.utils.factory.AdminFactory;
import com.example.app.utils.factory.UserFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;

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
@Sql(scripts = {"/testSql/schema.sql", "/testSql/data.sql"})
@Sql(scripts = "/testSql/delete.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
@SpringBootTest(classes = RestMarketPlaceAppApplication.class)
class CategoryControllerTest {

    private static final String BASE_URL = "/api/v1/category";

    @Autowired
    WebApplicationContext webAppContext;
    @Autowired
    ObjectMapper mapper;

    MockMvc mockMvc;
    String jwtToken;
    CategoryDTO request;
    Category response;

    @BeforeEach
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext)
                .apply(SecurityMockMvcConfigurers.springSecurity()).build();

        UserFactory factory = new AdminFactory();
        User user = factory.createUser();

        jwtToken = TokenUtil.createToken(user);

        request = new CategoryDTO("tablet computer",
                "is a mobile device, typically with a mobile operating system and touchscreen display " +
                        "processing circuitry, and a rechargeable battery in a single.");
        response = Category.builder()
                .id(3L)
                .name("tablet computer")
                .description("is a mobile device, typically with a mobile operating system and touchscreen display " +
                        "processing circuitry, and a rechargeable battery in a single.")
                .build();
    }

    @Test
    void addProductToMarketShouldReturnCreated() throws Exception {

        this.mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
                        .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().string(mapper.writeValueAsString(response)));
    }

    @Test
    void addAlreadyExistsProductShouldReturnConflict() throws Exception {

        request = new CategoryDTO("laptop",
                "A laptop, sometimes called a notebook computer by manufacturers, is a battery- "
                        + "or AC-powered personal computer (PC) smaller than a briefcase.");

        response = Category.builder()
                .id(2L)
                .name("laptop")
                .description(
                        "A laptop, sometimes called a notebook computer by manufacturers, is a battery-"
                                + " or AC-powered personal computer (PC) smaller than a briefcase.")
                .build();

        final ErrorResponse errorResponse = new ErrorResponse(
                ApplicationExceptionHandler.DUPLICATE_ENTRY,
                String.format("Category [%s] already exists", request.getName()));

        this.mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
                        .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(
                        MockMvcResultMatchers.content().string(mapper.writeValueAsString(errorResponse)));
    }

}
