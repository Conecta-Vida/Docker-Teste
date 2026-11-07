package br.edu.ifsp.conectaavida.admin.controller;

import br.edu.ifsp.conectaavida.core.domain.InstituicaoSaude;
import br.edu.ifsp.conectaavida.core.repository.InstituicaoSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    // Usado pelo painel admin V2 (botão "Editar")
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody InstituicaoSaude dados) {
        return repository.findById(id).map(existente -> {
            dados.setId(id);
            return ResponseEntity.ok(repository.save(dados));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Usado pelo painel admin V2 (botão "Excluir")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            repository.deleteById(id);
            repository.flush();
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("erro", "Esta unidade possui comunicações vinculadas e não pode ser excluída."));
        }
    }
}
