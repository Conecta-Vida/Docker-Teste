package br.edu.ifsp.conectaavida.admin.controller;

import br.edu.ifsp.conectaavida.core.domain.InstituicaoSaude;
import br.edu.ifsp.conectaavida.core.repository.InstituicaoSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CONTROLLER DE INSTITUIÇÕES DE SAÚDE
 * Gerencia os Hospitais, UBS e Postos cadastrados no sistema.
 */
@RestController
@RequestMapping("/api/instituicoes")
public class InstituicaoSaudeController {

    @Autowired
    private InstituicaoSaudeRepository repository;

    @GetMapping
    public ResponseEntity<List<InstituicaoSaude>> listarTodas() {
        return ResponseEntity.ok(repository.findAll());
    }

    @PostMapping
    public ResponseEntity<InstituicaoSaude> cadastrar(@RequestBody InstituicaoSaude instituicao) {
        InstituicaoSaude salva = repository.save(instituicao);
        return ResponseEntity.ok(salva);
    }
}