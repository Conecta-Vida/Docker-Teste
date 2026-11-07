package br.edu.ifsp.conectaavida.mobile.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS da API do app. O app nativo (Android/iOS) não usa CORS, mas o Flutter
 * rodando no navegador (flutter run -d chrome, porta aleatória) precisa.
 * Aceita padrões como http://localhost:* (variável CONECTA_CORS_ORIGINS).
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.security.allowed-origins:http://localhost:*}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
