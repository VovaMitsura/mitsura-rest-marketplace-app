package com.example.app.controller;

import com.example.app.RestMarketPlaceAppApplication;
import com.example.app.controller.dto.DiscountDTO;
import com.example.app.controller.dto.ProductDTO;
import com.example.app.exception.ApplicationExceptionHandler;
import com.example.app.exception.ApplicationExceptionHandler.ErrorResponse;
import com.example.app.model.Product;
import com.example.app.model.User;
import com.example.app.repository.ProductRepository;
import com.example.app.repository.UserRepository;
import com.example.app.security.JwtAuthenticationFilter;
import com.example.app.utils.TokenUtil;
import com.example.app.utils.factory.SellerFactory;
import com.example.app.utils.factory.UserFactory;
import com.fasterxml.jackson.databind.JsonNode;
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

    private MockMvc mockMvc;
    private String jwtToken;
    private ProductDTO postProduct;
    private Product responseProduct;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext)
                .addFilter(authenticationFilter).
                build();

        postProduct = new ProductDTO();
        postProduct.setName("Samsung a71");
        postProduct.setDiscount(null);
        postProduct.setPrice(18000);
        postProduct.setQuantity(40);

        responseProduct = new Product();
        responseProduct.setId(5L);
        responseProduct.setName(postProduct.getName());
        responseProduct.setPrice(postProduct.getPrice());
        responseProduct.setQuantity(postProduct.getQuantity());

        UserFactory factory = new SellerFactory();
        User user = factory.createUser();

        jwtToken = TokenUtil.createToken(user);
    }

    @Test
    @Order(5)
    void postValidProductShouldReturnCreated() throws Exception {

        postProduct.setCategory("smartphone");
        postProduct.setPriceInBonus(1200);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(SELLER_URL + "/products")
                        .content(objectMapper.writeValueAsString(postProduct))
                        .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());

        Product response = new Product(node.get("id").asLong(), node.get("name").asText(),
                node.get("price").asInt(), node.get("priceInBonus").asInt(), null, null,
                null, null, node.get("quantity").asInt(), null);

        Assertions.assertEquals(responseProduct.getId(), response.getId());
        Assertions.assertEquals(responseProduct.getName(), response.getName());
        Assertions.assertEquals(responseProduct.getCategory(), response.getCategory());
        Assertions.assertEquals(responseProduct.getPrice(), response.getPrice());
        Assertions.assertEquals(responseProduct.getCategory(), response.getCategory());
        Assertions.assertEquals(responseProduct.getPriceInBonus(), response.getPriceInBonus());
    }

    @Test
    @Order(2)
    void postProductWithInvalidDiscountShouldReturnNotFound() throws Exception {
        postProduct.setDiscount("lol");
        postProduct.setCategory("smartphone");
        postProduct.setPriceInBonus(1200);
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
        postProduct.setPriceInBonus(1200);
        ErrorResponse response = new ErrorResponse(ApplicationExceptionHandler.CATEGORY_NOT_FOUND,
                String.format("category with name: %s not found", postProduct.getCategory()));

        mockMvc.perform(
                        MockMvcRequestBuilders.post(SELLER_URL + "/products").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(postProduct))
                                .accept(MediaType.APPLICATION_JSON)
                                .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
                .andExpect(MockMvcResultMatchers.status().isNotFound()).andExpect(
                        MockMvcResultMatchers.content().string(objectMapper.writeValueAsString(response)));
    }

    @Test
    @Order(4)
    void getAllCustomersShouldReturnOneValue() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get(SELLER_URL)
                        .header(TokenUtil.AUTH_HEADER, TokenUtil.TOKEN_PREFIX + jwtToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
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
        ProductDTO request = new ProductDTO("Samsung m53", 16000, 1500, null, "smartphone", null, 5);

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
        ProductDTO request = new ProductDTO("Samsung m53", 16000, 1200, null, "smartphone",
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

}
