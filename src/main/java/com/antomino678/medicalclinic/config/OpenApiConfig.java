package com.antomino678.medicalclinic.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI medicalClinicOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Medical Clinic API")
                .description("API for managing users, patients, doctors, facilities and visits")
                .version("1.0"));
    }
}
