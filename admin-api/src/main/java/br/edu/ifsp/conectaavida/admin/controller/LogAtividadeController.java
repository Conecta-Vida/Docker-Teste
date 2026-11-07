package br.edu.ifsp.conectaavida.admin.controller;

import br.edu.ifsp.conectaavida.core.domain.LogAtividade;
import br.edu.ifsp.conectaavida.core.repository.LogAtividadeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * CONTROLLER DE AUDITORIA
 * Permite que o administrador-chefe visualize a lista de ações na tela do painel.
 */
@RestController
@RequestMapping("/api/logs")
public class LogAtividadeController {

    @Autowired
    private LogAtividadeRepository logRepository;

    @GetMapping
    public ResponseEntity<List<LogAtividade>> listarLogs() {
        return ResponseEntity.ok(logRepository.findAll());
    }
}