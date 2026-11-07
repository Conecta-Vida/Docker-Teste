package br.edu.ifsp.conectaavida.service;

import br.edu.ifsp.conectaavida.domain.DispositivoPush;
import br.edu.ifsp.conectaavida.repository.DispositivoPushRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Serviço de envio de Push Notifications via Firebase Cloud Messaging (FCM).
 *
 * Usa a API legada do FCM (HTTP v1 simplificado via Server Key).
 * Para ativar: adicione fcm.server.key no application.properties com a
 * Server Key do seu projeto Firebase Console.
 *
 * Como obter a Server Key:
 * 1. Acesse console.firebase.google.com
 * 2. Selecione o projeto
 * 3. Configurações do Projeto → Cloud Messaging
 * 4. Copie a "Server key" (seção API do Cloud Messaging)
 */
@Service
public class FcmService {

    @Value("${fcm.server.key:}")
    private String serverKey;

    private final DispositivoPushRepository dispositivoPushRepository;

    public FcmService(DispositivoPushRepository dispositivoPushRepository) {
        this.dispositivoPushRepository = dispositivoPushRepository;
    }

    private static final String FCM_URL =
            "https://fcm.googleapis.com/fcm/send";

    /**
     * Dispara push notification para TODOS os dispositivos registrados.
     *
     * @param titulo  Título da notificação
     * @param corpo   Corpo/descrição da notificação
     * @param dados   Dados extras opcionais (ex: tipo, id do alerta)
     */
    public void enviarParaTodos(String titulo, String corpo, Map<String, String> dados) {
        Set<String> tokens = dispositivoPushRepository.findAll().stream()
                .map(DispositivoPush::getToken)
                .collect(Collectors.toSet());
        if (tokens.isEmpty()) {
            System.out.println("⚠️ FCM: Nenhum dispositivo registrado para receber push.");
            return;
        }

        if (serverKey == null || serverKey.isBlank()) {
            System.out.println("⚠️ FCM: Server Key não configurada. Defina a variável de ambiente FCM_SERVER_KEY.");
            return;
        }

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "key=" + serverKey);

        // Envia em lote para todos os tokens registrados
        for (String token : tokens) {
            try {
                Map<String, Object> payload = Map.of(
                        "to", token,
                        "priority", "high",
                        "notification", Map.of(
                                "title", titulo,
                                "body", corpo,
                                "sound", "default"
                        ),
                        "data", dados != null ? dados : Map.of()
                );

                HttpEntity<Map<String, Object>> request =
                        new HttpEntity<>(payload, headers);

                ResponseEntity<String> response = restTemplate.postForEntity(
                        FCM_URL, request, String.class
                );

                System.out.println("✅ FCM enviado para " +
                        token.substring(0, Math.min(20, token.length())) + "... Status: " +
                        response.getStatusCode());
            } catch (Exception e) {
                System.err.println("❌ FCM: Erro ao enviar para " +
                        token.substring(0, Math.min(20, token.length())) +
                        ": " + e.getMessage());
            }
        }
    }
}
