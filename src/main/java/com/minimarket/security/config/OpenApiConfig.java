package com.minimarket.security.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "MiniMarket Plus API",
        version = "1.8.0",
        description = "API REST para gestión de minimarket. Autenticación mediante JWT — obtén el token en POST /auth/login y úsalo en el botón Authorize."
    )
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Ingresa el token JWT obtenido en /auth/login"
)
public class OpenApiConfig {
}