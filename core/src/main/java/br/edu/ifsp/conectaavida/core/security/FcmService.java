package br.edu.ifsp.conectaavida.core.security;

import br.edu.ifsp.conectaavida.core.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.HashMap;

/**
 * SERVIÇO DE NOTIFICAÇÕES ASSÍNCRONAS (RabbitMQ)
 *
 * Firebase removido. Esta classe adota o padrão orientado a eventos (Fase 3 da Arquitetura).
 * A thread HTTP é liberada imediatamente, evitando gargalos de escalabilidade.
 */
@Service
public class FcmService {

    @Autowired
    private RabbitTemplate rabbitTemplate; // Ferramenta do Spring para enviar mensagens à fila

    /**
     * 1. PRODUTOR (Chamado pelos Controllers de Alerta/Campanha)
     * Não trava o servidor. Apenas empacota os dados e joga na fila do RabbitMQ.
     */
    public void enviarParaTodos(String titulo, String corpo, Map<String, String> dados) {

        Map<String, String> payload = new HashMap<>();
        payload.put("titulo", titulo);
        payload.put("corpo", corpo);

        // Envia para a esteira em background
        rabbitTemplate.convertAndSend(RabbitMQConfig.FILA_NOTIFICACOES, payload);

        System.out.println("🚀 [PRODUTOR] Tarefa de notificação enviada para a fila com sucesso!");
    }

    /**
     * 2. CONSUMIDOR (WORKER EM BACKGROUND)
     * Fica escutando a fila invisivelmente. Quando chega uma mensagem, ele a processa
     * sem afetar o tempo de resposta do cidadão ou do gestor.
     */
    @RabbitListener(queues = RabbitMQConfig.FILA_NOTIFICACOES)
    public void processarFilaEmBackground(Map<String, String> payload) {

        String titulo = payload.get("titulo");
        String corpo = payload.get("corpo");

        // Simulação do envio (O Firebase foi erradicado do projeto)
        System.out.println("🔔 [WORKER ASSÍNCRONO] Processando notificação em lote...");
        System.out.println("➡️ Título: " + titulo);
        System.out.println("➡️ Corpo: " + corpo);
        System.out.println("✅ Disparo simulado concluído perfeitamente!");
    }
}