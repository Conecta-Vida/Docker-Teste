package br.edu.ifsp.conectaavida.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * MOTOR DE ARRANQUE DA ADMIN API
 *
 * Explicação para a Equipe:
 * Como dividimos o projeto, precisamos avisar ao Spring Boot para sair da pasta
 * "admin" e ir procurar as Entidades e Repositórios lá na pasta "core".
 */
@SpringBootApplication
@EnableCaching // 🚀 ATIVA O MOTOR DE CACHE (FASE 2)
@EntityScan(basePackages = "br.edu.ifsp.conectaavida.core.domain") // Encontra as Tabelas
@EnableJpaRepositories(basePackages = "br.edu.ifsp.conectaavida.core.repository") // Encontra os Repositórios
@ComponentScan(basePackages = {
        "br.edu.ifsp.conectaavida.admin", // Lê os Controllers do Admin
        "br.edu.ifsp.conectaavida.core"   // Lê os Services do Core (Criptografia, FCM, etc)
})
public class AdminApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminApiApplication.class, args);
    }

}