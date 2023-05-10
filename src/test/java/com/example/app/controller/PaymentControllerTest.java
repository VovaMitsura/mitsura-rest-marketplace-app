package com.example.app.controller;

import com.example.app.RestMarketPlaceAppApplication;
import com.example.app.controller.dto.PaymentRequestDTO;
import com.example.app.controller.dto.StipePaymentResponseDTO;
import com.example.app.exception.ApplicationExceptionHandler;
import com.example.app.model.Order;
import com.example.app.model.*;
import com.example.app.security.JwtAuthenticationFilter;
import com.example.app.service.OrderService;
import com.example.app.service.ProductService;
import com.example.app.service.UserService;
import com.example.app.service.stripe.StripePaymentService;
import com.example.app.utils.TokenUtil;
import com.example.app.utils.factory.CustomerFactory;
import com.example.app.utils.factory.UserFactory;
import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

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
    @Autowired
    UserService userService;
    @Autowired
    ProductService productService;

    @MockBean
    StripePaymentService paymentService;

    private MockMvc mockMvc;
    private String jwtToken;
    private PaymentRequestDTO request;
    private StipePaymentResponseDTO response;
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
        charge.setStatus("succeeded");
        charge.setAmount(225L);
        Mockito.doReturn(charge).when(paymentService).pay(Mockito.any(CreditCard.class), Mockito.any(Order.class));

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();


        JsonNode node = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        String responseMessage = node.get("message").asText();

        List<Order> userOrders = orderService.getUserOrders(user.getEmail(), Order.Status.BOUGHT);

        List<Bonus> bonuses = new ArrayList<>();

        for (OrderDetails details : userOrders.get(0).getOrderDetails()) {
            Product ordederProduct = details.getProduct();
            Product productInMarket = productService.getProductById(ordederProduct.getId());

            if (productInMarket.getBonus() != null)
                bonuses.add(productInMarket.getBonus());
        }

        int sumOfBonuses = bonuses.stream().mapToInt(Bonus::getAmount).sum();
        User customer = userService.getUserByEmail(user.getEmail());

        Assertions.assertNotNull(responseMessage);
        Assertions.assertEquals("Payment is succeed", responseMessage);
        Assertions.assertEquals(Order.Status.BOUGHT, userOrders.get(0).getStatus());
        Assertions.assertEquals(sumOfBonuses, customer.getTotalBonusAmount());
    }

    @Test
    void payForOneOrderTwoTimesThrowException() throws Exception {
        Charge charge = new Charge();
        charge.setStatus("succeeded");
        charge.setAmount(225L);
        Mockito.doReturn(charge).when(paymentService).pay(Mockito.any(CreditCard.class), Mockito.any(Order.class));

        this.mockMvc.perform(MockMvcRequestBuilders.post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
                .andExpect(MockMvcResultMatchers.status().isOk());

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        var response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                ApplicationExceptionHandler.ErrorResponse.class);

        Assertions.assertEquals(ApplicationExceptionHandler.ORDER_NOT_FOUND, response.getErrorCode());
        Assertions.assertEquals(String.format("No order of user [%s] with id [%d]", user.getEmail(), request.getOrderID()),
                response.getErrorMessage());
    }
}