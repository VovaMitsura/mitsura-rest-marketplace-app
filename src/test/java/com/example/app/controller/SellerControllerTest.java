package com.example.app.controller;

import com.example.app.RestMarketPlaceAppApplication;
import com.example.app.controller.dto.DiscountDTO;
import com.example.app.controller.dto.ProductDTO;
import com.example.app.controller.dto.SellerDTO;
import com.example.app.controller.dto.SellerStatistic;
import com.example.app.exception.ApplicationExceptionHandler;
import com.example.app.exception.ApplicationExceptionHandler.ErrorResponse;
import com.example.app.model.Bonus;
import com.example.app.model.Product;
import com.example.app.model.User;
import com.example.app.repository.BonusRepository;
import com.example.app.repository.ProductRepository;
import com.example.app.repository.UserRepository;
import com.example.app.security.JwtAuthenticationFilter;
import com.example.app.service.ProductService;
import com.example.app.utils.TokenUtil;
import com.example.app.utils.factory.SellerFactory;
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
@SpringBootTest(classes = RestMarketPlaceAppApplication.class)
class SellerControllerTest {

    private final String SELLER_URL = "/api/v1/sellers";
    @Autowired
    private WebApplicationContext webAppContext;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private JwtAuthenticationFilter authenticationFilter;
    @Autowired
    private BonusRepository bonusRepository;

    private MockMvc mockMvc;
    private String jwtToken;
    private ProductDTO postProduct;
    private User user;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext)
                .addFilter(authenticationFilter).
                build();

        postProduct = new ProductDTO();
        postProduct.setName("Samsung a71");
        postProduct.setBonus("action");
        postProduct.setDiscount(null);
        postProduct.setPrice(18000);
        postProduct.setQuantity(40);

        UserFactory factory = new SellerFactory();
        user = factory.createUser();

        jwtToken = TokenUtil.createToken(user);
    }

    @Test
    @Order(5)
    void postValidProductShouldReturnCreated() throws Exception {

        postProduct.setCategory("smartphone");

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(SELLER_URL + "/products")
                        .content(objectMapper.writeValueAsString(postProduct))
                        .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();

        Product response = objectMapper.readValue(result.getResponse().getContentAsString(), Product.class);
        Bonus bonus = bonusRepository.findByName(postProduct.getBonus()).get();

        Assertions.assertNotNull(response.getId());
        Assertions.assertEquals(postProduct.getName(), response.getName());
        Assertions.assertEquals(postProduct.getPrice(), response.getPrice());
        Assertions.assertEquals(postProduct.getQuantity(), response.getQuantity());
        Assertions.assertEquals(postProduct.getBonus(), response.getBonus().getName());
        Assertions.assertEquals(postProduct.getCategory(), response.getCategory().getName());
        Assertions.assertEquals(bonus.getId(), response.getBonus().getId());
        Assertions.assertEquals(bonus.getName(), response.getBonus().getName());
        Assertions.assertEquals(bonus.getAmount(), response.getBonus().getAmount());
    }

    @Test
    @Order(2)
    void postProductWithInvalidDiscountShouldReturnNotFound() throws Exception {
        postProduct.setDiscount("lol");
        postProduct.setCategory("smartphone");
        ErrorResponse response = new ErrorResponse(ApplicationExceptionHandler.DISCOUNT_NOT_FOUND,
                String.format("discount with name: '%s' not found", postProduct.getDiscount()));

        mockMvc.perform(MockMvcRequestBuilders.post(SELLER_URL + "/products")
                        .content(objectMapper.writeValueAsString(postProduct))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                        .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
                .andExpect(MockMvcResultMatchers.status().isNotFound()).andExpect(
                        MockMvcResultMatchers.content().string(objectMapper.writeValueAsString(response)));
    }

    @Test
    @Order(3)
    void postProductWithInvalidCategoryShouldReturnNotFound() throws Exception {
        postProduct.setCategory("lol");
        ErrorResponse response = new ErrorResponse(ApplicationExceptionHandler.CATEGORY_NOT_FOUND,
                String.format("category with name: %s not found", postProduct.getCategory()));

        mockMvc.perform(MockMvcRequestBuilders.post(SELLER_URL + "/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postProduct))
                        .accept(MediaType.APPLICATION_JSON)
                        .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
                .andExpect(MockMvcResultMatchers.status().isNotFound()).andExpect(
                        MockMvcResultMatchers.content().string(objectMapper.writeValueAsString(response)));
    }

    @Test
    @Order(4)
    void getAllCustomersShouldReturnOneValue() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get(SELLER_URL)
                        .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        List<SellerDTO> response = List.of(
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), SellerDTO[].class));
        List<SellerDTO> sellers = userRepository.getUsersByRole(User.Role.SELLER).stream().map(SellerDTO::new).toList();

        Assertions.assertEquals(sellers.get(0).getId(), response.get(0).getId());
        Assertions.assertEquals(sellers.get(0).getRole(), response.get(0).getRole());
        Assertions.assertEquals(sellers.get(0).getFullName(), response.get(0).getFullName());
        Assertions.assertEquals(sellers.get(0).getEmail(), response.get(0).getEmail());
        Assertions.assertEquals(sellers.get(0).getProducts().size(), response.get(0).getProducts().size());
    }

    @Test
    @Order(1)
    void getCustomerByIdShouldReturnOneValues() throws Exception {
        User byEmail = userRepository.findByEmail("tanya@mail.com").get();

        this.mockMvc.perform(
                        MockMvcRequestBuilders.get(SELLER_URL + String.format("/%d", byEmail.getId()))
                                .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void updateProductShouldReturnAccepted() throws Exception {
        ProductDTO request = new ProductDTO("Samsung m53", 16000, null, "smartphone", null, 5);

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.put(SELLER_URL + "/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
                .andExpect(MockMvcResultMatchers.status().isAccepted())
                .andReturn();

        Product response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Product.class);

        Assertions.assertEquals(1L, response.getId());
        Assertions.assertEquals(request.getPrice(), response.getPrice());
        Assertions.assertEquals(request.getName(), response.getName());
        Assertions.assertEquals(request.getQuantity(), response.getQuantity());
    }

    @Test
    void updateNonExistingProductShouldReturnException() throws Exception {
        ProductDTO request = new ProductDTO("Samsung m53", 16000, null, "smartphone",
                null, 5);

        ErrorResponse errorResponse = new ErrorResponse(ApplicationExceptionHandler.PRODUCT_NOT_FOUND,
                String.format("User with email [%s] has no product with id [%d]", "tanya@mail.com", 10L));

        this.mockMvc.perform(MockMvcRequestBuilders.put(SELLER_URL + "/products/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string(objectMapper.writeValueAsString(errorResponse)));
    }

    @Test
    void deleteProductShouldReturnOk() throws Exception {
        Product current = productRepository.findById(1L).orElseThrow();

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.delete(SELLER_URL + "/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        Product response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Product.class);

        Assertions.assertEquals(response.getId(), current.getId());
        Assertions.assertEquals(response.getName(), current.getName());
        Assertions.assertEquals(response.getPrice(), current.getPrice());
    }

    @Test
    void deleteNonExistingProductShouldReturnException() throws Exception {

        ErrorResponse errorResponse = new ErrorResponse(ApplicationExceptionHandler.PRODUCT_NOT_FOUND,
                String.format("User with email [%s] has no product with id [%d]", "tanya@mail.com", 10L));

        this.mockMvc.perform(MockMvcRequestBuilders.delete(SELLER_URL + "/products/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string(objectMapper.writeValueAsString(errorResponse)));

    }

    @Test
    void addDiscountToProductShouldReturnOK() throws Exception {

        DiscountDTO discountDTO = new DiscountDTO("Happy New Year disc.", 30);

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post(SELLER_URL + "/products/1/discounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(discountDTO))
                        .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ProductDTO productDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ProductDTO.class);
        Product product = productRepository.findById(1L).get();

        Assertions.assertNotNull(productDTO);
        Assertions.assertEquals(productDTO.getDiscount(), product.getDiscount().getName());
        Assertions.assertEquals(product.getDiscount().getDiscountPercent(), discountDTO.getPercentage());
    }


    @Test
    void getSellerStatisticShouldReturnStatusOk() throws Exception {

        Product product2 = productRepository.findByIdAndSellerEmail(2l, user.getEmail()).get();
        ProductDTO productDTO2 = ProductDTO.builder()
                .name(product2.getName())
                .price(product2.getPrice())
                .quantity(product2.getQuantity())
                .discount(product2.getDiscount() == null ? null : product2.getDiscount().getName())
                .category(product2.getCategory().getName())
                .bonus(product2.getBonus() == null ? null : product2.getBonus().getName())
                .build();

        SellerStatistic.CustomerStat customerStat = new SellerStatistic.CustomerStat(3L, "Dan Samuel",
                "Samuel@mail.com", User.Role.CUSTOMER, 0,
                com.example.app.model.Order.Status.BOUGHT);

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get(
                                SELLER_URL + "/products/statistics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        List<SellerStatistic> response = List.of(objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                SellerStatistic[].class));

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.stream().anyMatch(sellerStatistic -> sellerStatistic.getProduct()
                .equals(productDTO2)));
        Assertions.assertTrue(response.stream().flatMap(sellerStatistic -> sellerStatistic.getCustomers().stream())
                .anyMatch(customer -> customer.equals(customerStat)));
    }

}
