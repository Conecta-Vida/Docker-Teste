package br.edu.ifsp.conectaavida.controller;

import br.edu.ifsp.conectaavida.domain.Comunicacao;
import br.edu.ifsp.conectaavida.repository.ComunicacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * CONTROLLER: NoticiaController
 * Rota Base: /api/noticias
 */
@RestController
@RequestMapping("/api/noticias")
public class NoticiaController {

    @Autowired private ComunicacaoRepository repository;

    @GetMapping
    public List<Comunicacao> listarTodas() {
        return repository.findByTipo("NOTICIA");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Comunicacao> buscarPorId(@PathVariable Long id) {
        return repository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Comunicacao criar(@RequestBody Comunicacao comunicacao) {
        comunicacao.setTipo("NOTICIA"); // Vincula rigidamente o tipo de postagem
        return repository.save(comunicacao);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Comunicacao> atualizar(@PathVariable Long id, @RequestBody Comunicacao dados) {
        return repository.findById(id).map(comunicacao -> {
            comunicacao.setTitulo(dados.getTitulo());
            comunicacao.setDescricao(dados.getDescricao());
            comunicacao.setCategoria(dados.getCategoria());
            comunicacao.setLinkimagem(dados.getLinkimagem());
            comunicacao.setLocalizacao(dados.getLocalizacao());
            comunicacao.setDataFim(dados.getDataFim());
            comunicacao.setDataInicio(dados.getDataInicio());
            return ResponseEntity.ok(repository.save(comunicacao));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}