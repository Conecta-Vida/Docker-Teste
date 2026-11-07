package br.edu.ifsp.conectaavida.mensageria;

import br.edu.ifsp.conectaavida.domain.Comunicacao;
import br.edu.ifsp.conectaavida.service.FcmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Publica eventos de comunicação.
 * - Mensageria ligada (Swarm): envia para o RabbitMQ e a mobile-api dispara o push.
 * - Mensageria desligada (dev local) ou RabbitMQ fora do ar: dispara o push direto,
 *   para o alerta nunca se perder.
 */
@Service
public class EventoPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventoPublisher.class);

    private final ObjectProvider<RabbitTemplate> rabbitTemplate;
    private final FcmService fcmService;
    private final boolean mensageriaAtiva;

    public EventoPublisher(ObjectProvider<RabbitTemplate> rabbitTemplate,
                           FcmService fcmService,
                           @Value("${app.mensageria.enabled:false}") boolean mensageriaAtiva) {
        this.rabbitTemplate = rabbitTemplate;
        this.fcmService = fcmService;
        this.mensageriaAtiva = mensageriaAtiva;
    }

    public void publicarAlerta(Comunicacao alerta) {
        EventoComunicacao evento = new EventoComunicacao(
                alerta.getId(), alerta.getTipo(), alerta.getTitulo(), alerta.getDescricao());

        RabbitTemplate template = rabbitTemplate.getIfAvailable();
        if (mensageriaAtiva && template != null) {
            try {
                template.convertAndSend(MensageriaConfig.EXCHANGE, MensageriaConfig.ROTA_ALERTA, evento);
                log.info("Evento de alerta {} publicado no RabbitMQ.", alerta.getId());
                return;
            } catch (Exception e) {
                log.warn("RabbitMQ indisponível ({}). Enviando push diretamente.", e.getMessage());
            }
        }
        enviarPush(evento);
    }

    /** Usado pelo consumidor da fila (mobile-api) e pelo fallback local. */
    public void enviarPush(EventoComunicacao evento) {
        fcmService.enviarParaTodos(
                "🚨 " + evento.titulo(),
                evento.descricao() != null ? evento.descricao() : "Alerta de saúde pública.",
                Map.of("tipo", String.valueOf(evento.tipo()), "id", String.valueOf(evento.id()))
        );
    }
}
