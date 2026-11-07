package br.edu.ifsp.conectaavida.mensageria;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TOPOLOGIA DO RABBITMQ
 *
 *   admin-api --(comunicacao.alerta)--> [exchange conectavida.eventos]
 *                                               |
 *                                   [fila conectavida.push]
 *                                               |
 *                                          mobile-api  --> Firebase (push)
 *
 * Com várias réplicas da mobile-api, o RabbitMQ entrega cada mensagem para
 * apenas UMA delas (competing consumers), evitando push duplicado.
 */
@Configuration
@ConditionalOnProperty(name = "app.mensageria.enabled", havingValue = "true")
public class MensageriaConfig {

    public static final String EXCHANGE = "conectavida.eventos";
    public static final String FILA_PUSH = "conectavida.push";
    public static final String ROTA_ALERTA = "comunicacao.alerta";

    @Bean
    public TopicExchange eventosExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue filaPush() {
        return QueueBuilder.durable(FILA_PUSH).build();
    }

    @Bean
    public Binding bindingPush(Queue filaPush, TopicExchange eventosExchange) {
        return BindingBuilder.bind(filaPush).to(eventosExchange).with("comunicacao.#");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
