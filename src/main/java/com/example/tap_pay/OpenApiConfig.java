package com.example.tap_pay;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI tapPayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TapPay Offline Payment API")
                        .description(
                                "Backend API for TapPay offline NFC payments, " +
                                        "wallet management, transaction synchronization " +
                                        "and settlement."
                        )
                        .version("1.0.0"));
    }
}