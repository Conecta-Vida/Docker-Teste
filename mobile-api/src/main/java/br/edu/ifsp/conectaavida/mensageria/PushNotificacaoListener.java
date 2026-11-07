package br.edu.ifsp.conectaavida.mensageria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * CONSUMIDOR RABBITMQ (somente na mobile-api).
 * Recebe os alertas publicados pela admin-api e dispara o push para os celulares.
 */
@Component
@ConditionalOnProperty(name = "app.mensageria.enabled", havingValue = "true")
public class PushNotificacaoListener {

    private static final Logger log = LoggerFactory.getLogger(PushNotificacaoListener.class);

    private final EventoPublisher eventoPublisher;

    public PushNotificacaoListener(EventoPublisher eventoPublisher) {
        this.eventoPublisher = eventoPublisher;
    }

    @RabbitListener(queues = MensageriaConfig.FILA_PUSH)
    public void aoReceberAlerta(EventoComunicacao evento) {
        log.info("Alerta {} recebido da fila. Disparando push...", evento.id());
        eventoPublisher.enviarPush(evento);
    }
}
