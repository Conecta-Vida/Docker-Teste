package br.edu.ifsp.conectaavida.admin.controller;

import br.edu.ifsp.conectaavida.core.security.FcmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * CONTROLLER DE NOTIFICAÇÕES DE CAMPANHAS
 *
 * Explicação para a Equipe:
 * Desacoplamos (separamos) a lógica de notificação para manter o código limpo.
 * Este controller serve especificamente para o gestor apertar um botão de "Avisar Inscritos".
 */
@RestController
@RequestMapping("/api/campanhas-notificacao")
public class CampanhaNotificacaoController {

    @Autowired
    private FcmService fcmService;

    @PostMapping("/{id}/notificar-fim")
    public ResponseEntity<String> notificarFimCampanha(@PathVariable Long id) {
        // Envia o Push Notification via Firebase informando que a campanha/mutirão está acabando
        fcmService.enviarParaTodos(
                "Atenção! Campanha Encerrando",
                "O mutirão de saúde que você se inscreveu está próximo do fim. Não perca!",
                null
        );
        return ResponseEntity.ok("Notificações enviadas aos inscritos.");
    }
}