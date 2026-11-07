package br.edu.ifsp.conectaavida;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API do APLICATIVO DO CIDADÃO (Flutter).
 * Rotas exclusivas do app: registro de dispositivos para push e o consumidor
 * da fila RabbitMQ que dispara as notificações. As rotas comuns (auth,
 * usuários, comunicações e inscrições) vêm do módulo :core.
 */
@SpringBootApplication
public class MobileApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MobileApiApplication.class, args);
    }
}
