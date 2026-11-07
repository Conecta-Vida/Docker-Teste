package br.edu.ifsp.conectaavida.admin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI adminOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Conecta Vida - Admin API")
                        .description("Documentação interativa das rotas do Painel Gerencial (React).")
                        .version("1.0.0"));
    }
}