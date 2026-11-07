package br.edu.ifsp.conectaavida.core.security;

import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * SERVIÇO DE NOTIFICAÇÕES (simulado)
 *
 * O Firebase e a fila RabbitMQ foram descartados do projeto. O disparo é
 * registrado no log da API, mantendo o mesmo ponto de chamada para uma
 * integração futura com push real.
 */
@Service
public class FcmService {

    public void enviarParaTodos(String titulo, String corpo, Map<String, String> dados) {
        System.out.println("🔔 [NOTIFICAÇÃO] " + titulo + " - " + corpo
                + (dados != null && !dados.isEmpty() ? " " + dados : ""));
    }
}
