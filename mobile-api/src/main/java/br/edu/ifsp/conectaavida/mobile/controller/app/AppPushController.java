package br.edu.ifsp.conectaavida.mobile.controller.app;

import br.edu.ifsp.conectaavida.core.service.PushTokenRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Rota que o app Flutter chama ao iniciar (/api/push/registrar-token).
 * Usa o mesmo registro de dispositivos de /api/mobile/notificacoes/registrar.
 */
@RestController
@RequestMapping("/api/push")
public class AppPushController {

    private final PushTokenRegistry tokenRegistry;

    public AppPushController(PushTokenRegistry tokenRegistry) {
        this.tokenRegistry = tokenRegistry;
    }

    @PostMapping("/registrar-token")
    public ResponseEntity<?> registrarToken(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", "Token FCM é obrigatório."));
        }
        tokenRegistry.registrarToken(token);
        return ResponseEntity.ok(Map.of("mensagem", "Token registrado com sucesso."));
    }
}
