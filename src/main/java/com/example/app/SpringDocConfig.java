package com.example.app;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition
public class SpringDocConfig {

    @Bean
    public OpenAPI baseOpenApi(){
        return new OpenAPI().info(new Info().title("MarketPlace Doc").version("1.0.0")
                .description("Restful Api for marketplace, which allows you to create account," +
                        " login to the system, look for goods, make purchases, look history of purchases" +
                        " and has system of bonuses."));
    }
}
