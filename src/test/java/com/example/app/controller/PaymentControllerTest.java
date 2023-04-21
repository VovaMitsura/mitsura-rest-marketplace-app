package com.example.app.controller;

import com.example.app.RestMarketPlaceAppApplication;
import com.example.app.controller.dto.PaymentRequestDTO;
import com.example.app.controller.dto.PaymentResponseDTO;
import com.example.app.model.CreditCard;
import com.example.app.model.Order;
import com.example.app.model.User;
import com.example.app.security.JwtAuthenticationFilter;
import com.example.app.service.OrderService;
import com.example.app.service.stripe.StripePaymentService;
import com.example.app.utils.TokenUtil;
import com.example.app.utils.factory.CustomerFactory;
import com.example.app.utils.factory.SellerFactory;
import com.example.app.utils.factory.UserFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.Charge;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Sql(scripts = {"/testSql/schema.sql", "/testSql/data.sql"})
@Sql(scripts = "/testSql/delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SpringBootTest(classes = RestMarketPlaceAppApplication.class)
class PaymentControllerTest {

    private static final String URL = "/api/v1/pay";

    @Autowired
    private WebApplicationContext webAppContext;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JwtAuthenticationFilter authenticationFilter;
    @Autowired
    private OrderService orderService;

    @MockBean
    StripePaymentService paymentService;

    private MockMvc mockMvc;
    private String jwtToken;
    private PaymentRequestDTO request;
    private PaymentResponseDTO response;
    private User user;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext)
                .addFilter(authenticationFilter).
                build();

        UserFactory factory = new CustomerFactory();
        user = factory.createUser();

        request = new PaymentRequestDTO(1L, new CreditCard("4242424242424242",
                "2024", "4", "123"));

        jwtToken = TokenUtil.createToken(user);
    }

    @Test
    void payForOrderShouldReturnStatusOk() throws Exception {
        Charge charge = new Charge();
        charge.setStatus("succeed");
        charge.setAmount(225L);
        Mockito.doReturn(charge).when(paymentService).pay(Mockito.any(CreditCard.class), Mockito.any(Order.class));

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), PaymentResponseDTO.class);
        Order userOrder = orderService.getUserOrderById(request.getOrderID(), user.getEmail());

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getAmount());
        Assertions.assertNotNull(response.getOrder());
        Assertions.assertEquals("succeed", response.getStatus());
    }
}