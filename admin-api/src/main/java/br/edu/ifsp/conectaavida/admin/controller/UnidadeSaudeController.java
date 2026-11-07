package br.edu.ifsp.conectaavida.admin.controller;

import br.edu.ifsp.conectaavida.core.domain.InstituicaoSaude;
import br.edu.ifsp.conectaavida.core.repository.InstituicaoSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * CONTROLLER DE BUSCA ESPECÍFICA (UNIDADES)
 *
 * Retorna dados detalhados de uma unidade de saúde específica para edição.
 */
@RestController
@RequestMapping("/api/unidades")
public class UnidadeSaudeController {

    @Autowired
    private InstituicaoSaudeRepository repository;

    @GetMapping("/{id}")
    public ResponseEntity<InstituicaoSaude> buscarPorId(@PathVariable Long id) {
        Optional<InstituicaoSaude> unidade = repository.findById(id);

        // Forma elegante e moderna de fazer um IF/ELSE no Spring
        return unidade.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}