package br.edu.ifsp.conectaavida.mobile.controller;

import br.edu.ifsp.conectaavida.core.service.PushTokenRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * CONTROLLER DE REGISTRO DE DISPOSITIVOS MÓVEIS
 *
 * Explicação para a Equipe:
 * Quando o cidadão abre o App Flutter pela primeira vez, o celular dele gera
 * um "Token" (código único do aparelho). O App manda esse código pra cá,
 * e nós guardamos na memória. Quando a Admin API quiser mandar um alerta de saúde,
 * ela vai ler essa lista!
 */
@RestController
@RequestMapping("/api/mobile/notificacoes")
public class PushTokenController {

    @Autowired
    private PushTokenRegistry tokenRegistry;

    @PostMapping("/registrar")
    public ResponseEntity<String> registrarCelular(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");

        if (token != null && !token.trim().isEmpty()) {
            tokenRegistry.registrarToken(token);
            return ResponseEntity.ok("Celular registrado com sucesso para receber alertas!");
        }

        return ResponseEntity.badRequest().body("Token de dispositivo inválido.");
    }
}