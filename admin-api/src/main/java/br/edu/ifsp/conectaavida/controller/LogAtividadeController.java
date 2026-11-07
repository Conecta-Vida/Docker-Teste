package br.edu.ifsp.conectaavida.controller;

import br.edu.ifsp.conectaavida.domain.LogAtividade;
import br.edu.ifsp.conectaavida.repository.LogAtividadeRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * CONTROLLER: LogAtividadeController
 * Rota Base: /api/logs
 */
@RestController
@RequestMapping("/api/logs")
public class LogAtividadeController {

    private final LogAtividadeRepository repository;

    public LogAtividadeController(LogAtividadeRepository repository) {
        this.repository = repository;
    }

    // Adicionar dentro de LogAtividadeController.java
    @GetMapping
    public List<LogAtividade> listarTodasAsAtividades() {
        return repository.findAll();
    }

    @GetMapping("/recentes")
    public List<LogAtividade> listarRecentes() {
        return repository.findTop5ByOrderByDataHoraDesc();
    }
}