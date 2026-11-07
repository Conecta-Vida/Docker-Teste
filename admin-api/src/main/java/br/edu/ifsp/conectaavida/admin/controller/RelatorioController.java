package br.edu.ifsp.conectaavida.admin.controller;

import br.edu.ifsp.conectaavida.core.domain.LogAtividade;
import br.edu.ifsp.conectaavida.core.repository.LogAtividadeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    @Autowired
    private LogAtividadeRepository logAtividadeRepository;

    @GetMapping("/logs")
    public ResponseEntity<List<LogAtividade>> listarUltimosLogs() {
        List<LogAtividade> logs = logAtividadeRepository.findTop10ByOrderByDataHoraDesc();
        return ResponseEntity.ok(logs);
    }
}