package br.edu.ifsp.conectaavida;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API do PAINEL ADMINISTRATIVO (React).
 * Rotas exclusivas de gestão: dashboard, alertas, campanhas, instituições,
 * logs e relatórios. As rotas comuns (auth, usuários, comunicações e
 * inscrições) vêm do módulo :core.
 */
@SpringBootApplication
public class AdminApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminApiApplication.class, args);
    }
}
