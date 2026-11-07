package br.edu.ifsp.conectaavida.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * FILTRO CORS (CROSS-ORIGIN RESOURCE SHARING)
 * Explicação para o grupo: Por padrão, o navegador bloqueia que um site (React na porta 5173)
 * converse com uma API (Spring Boot na porta 8080) por estarem em portas diferentes.
 * 🟢 EVOLUÇÃO: Alterado de "*" para ler uma lista restrita de domínios via variáveis de ambiente,
 * impedindo que scripts maliciosos de outros sites acessem nossa API em produção.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    // 🟢 CORRIGIDO: Substituída a URL da API pela URL oficial do Frontend hospedado na Vercel
    @Value("${app.security.allowed-origins:http://localhost:5173,http://localhost:3000,http://localhost:4000,https://conecta-vida-admin.vercel.app}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Estende o direito de acesso para todas as URLs da API
                .allowedOrigins(allowedOrigins.split(",")) // Converte a string configurada em array de origens aceitas
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD") // Libera os verbos HTTP completos
                .allowedHeaders("*") // Permite o envio de qualquer cabeçalho de metadados
                .allowCredentials(true); // Permite troca segura de cookies/tokens de sessão
    }
}