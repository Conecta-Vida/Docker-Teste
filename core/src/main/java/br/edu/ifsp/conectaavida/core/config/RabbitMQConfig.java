package br.edu.ifsp.conectaavida.core.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String FILA_NOTIFICACOES = "fila_notificacoes_push";

    @Bean
    public Queue filaNotificacoes() {
        return new Queue(FILA_NOTIFICACOES, true);
    }

    // NOVA CONFIGURAÇÃO: Força o uso de JSON (seguro) em vez de serialização Java nativa
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}