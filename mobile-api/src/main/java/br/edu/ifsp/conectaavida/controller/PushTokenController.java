package br.edu.ifsp.conectaavida.controller;

import br.edu.ifsp.conectaavida.domain.DispositivoPush;
import br.edu.ifsp.conectaavida.repository.DispositivoPushRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Gerencia os tokens FCM dos dispositivos para envio de push notifications.
 * Os tokens ficam no banco (tabela dispositivos_push), assim todas as réplicas
 * da mobile-api e da admin-api enxergam a mesma lista.
 */
@RestController
@RequestMapping("/api/push")
public class PushTokenController {

    private final DispositivoPushRepository dispositivoPushRepository;

    public PushTokenController(DispositivoPushRepository dispositivoPushRepository) {
        this.dispositivoPushRepository = dispositivoPushRepository;
    }

    /**
     * Recebe e salva o token FCM enviado pelo app mobile ao iniciar.
     * POST /api/push/registrar-token
     */
    @PostMapping("/registrar-token")
    public ResponseEntity<?> registrarToken(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensagem", "Token FCM é obrigatório."));
        }

        DispositivoPush dispositivo = dispositivoPushRepository.findByToken(token)
                .orElseGet(DispositivoPush::new);
        dispositivo.setToken(token);
        dispositivo.setPlataforma(payload.getOrDefault("plataforma", "desconhecida"));
        dispositivo.setDataRegistro(LocalDateTime.now());
        try {
            dispositivoPushRepository.saveAndFlush(dispositivo);
        } catch (DataIntegrityViolationException e) {
            // Outra réplica registrou o mesmo token no mesmo instante: ele já está salvo
        }

        return ResponseEntity.ok(Map.of(
                "mensagem", "Token registrado com sucesso.",
                "total_dispositivos", dispositivoPushRepository.count()
        ));
    }

    /**
     * Lista os dispositivos registrados (tokens mascarados).
     * GET /api/push/dispositivos
     */
    @GetMapping("/dispositivos")
    public ResponseEntity<?> listarDispositivos() {
        return ResponseEntity.ok(Map.of(
                "total", dispositivoPushRepository.count(),
                "tokens", dispositivoPushRepository.findAll().stream()
                        .map(d -> d.getToken().substring(0, Math.min(20, d.getToken().length())) + "...")
                        .toList()
        ));
    }
}
