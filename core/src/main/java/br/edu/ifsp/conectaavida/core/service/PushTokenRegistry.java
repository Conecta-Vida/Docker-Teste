package br.edu.ifsp.conectaavida.core.service;

import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * REGISTRO CENTRAL DE DISPOSITIVOS (FCM)
 *
 * Explicação para a Equipe:
 * Como dividimos o projeto em duas APIs, precisamos de um lugar central para
 * guardar os tokens dos celulares em memória RAM.
 * A Mobile API vai adicionar os tokens aqui, e a Admin API vai ler os tokens daqui para enviar os alertas.
 */
@Component
public class PushTokenRegistry {

    // Thread-safe: Permite que milhares de celulares se registrem ao mesmo tempo sem travar a lista
    private final Set<String> tokensRegistrados = ConcurrentHashMap.newKeySet();

    public void registrarToken(String token) {
        if (token != null && !token.isBlank()) {
            tokensRegistrados.add(token);
        }
    }

    public Set<String> obterTodosOsTokens() {
        return tokensRegistrados;
    }
}