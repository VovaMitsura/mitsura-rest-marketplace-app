package com.example.app.controller;

import com.example.app.RestMarketPlaceAppApplication;
import com.example.app.controller.dto.GrantDTO;
import com.example.app.model.User;
import com.example.app.repository.UserRepository;
import com.example.app.utils.TokenUtil;
import com.example.app.utils.factory.AdminFactory;
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

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {RestMarketPlaceAppApplication.class})
@Sql(scripts = {"/testSql/schema.sql", "/testSql/data.sql"})
@Sql(scripts = "/testSql/delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class AdminControllerTest {

    private static final String URL = "/api/v1/admins";

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;
    private String jwt;


    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        UserFactory factory = new AdminFactory();
        User user = factory.createUser();

        jwt = TokenUtil.createToken(user);
    }

    @Test
    void grandCustomerRoleAdmin() throws Exception {
        User customer = userRepository.findByEmail("john@mail.com").get();
        String postURL = String.format("%s?email=%s&role=%s", URL, customer.getEmail(), User.Role.ADMIN);

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post(postURL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwt))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        User admin = userRepository.findByEmail("john@mail.com").get();
        GrantDTO response = mapper.readValue(mvcResult.getResponse().getContentAsString(), GrantDTO.class);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(User.Role.ADMIN, response.getRole());
        Assertions.assertEquals(User.Role.ADMIN, admin.getRole());
        Assertions.assertEquals(customer.getEmail(), response.getEmail());
        Assertions.assertEquals(customer.getEmail(), admin.getEmail());
    }

}