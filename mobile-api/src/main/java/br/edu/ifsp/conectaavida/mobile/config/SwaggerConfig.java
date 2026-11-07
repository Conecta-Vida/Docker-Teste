package br.edu.ifsp.conectaavida.mobile.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI mobileOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Conecta Vida - Mobile API")
                        .description("Endpoints otimizados para consumo no aplicativo Flutter.")
                        .version("1.0.0"));
    }
}
