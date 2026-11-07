package br.edu.ifsp.conectaavida.controller;

import br.edu.ifsp.conectaavida.domain.Comunicacao;
import br.edu.ifsp.conectaavida.repository.ComunicacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CONTROLLER: CampanhaController
 * Rota Base: /api/campanhas
 * Objetivo: Listagem e modificação pontual de mutirões de saúde pública.
 */
@RestController
@RequestMapping("/api/campanhas")
public class CampanhaController {

    @Autowired
    private ComunicacaoRepository comunicacaoRepository;

    @GetMapping
    public ResponseEntity<List<Comunicacao>> listarTodasCampanhas() {
        List<Comunicacao> campanhas = comunicacaoRepository.findByTipoOrderByDataInicioDesc("CAMPANHA");
        return ResponseEntity.ok(campanhas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Comunicacao> buscarPorId(@PathVariable Long id) {
        return comunicacaoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarCampanha(@PathVariable Long id, @RequestBody Comunicacao dadosNovos) {
        return comunicacaoRepository.findById(id)
                .map(campanha -> {
                    campanha.setTitulo(dadosNovos.getTitulo());
                    campanha.setDescricao(dadosNovos.getDescricao());
                    campanha.setCategoria(dadosNovos.getCategoria());
                    campanha.setPublicoAlvo(dadosNovos.getPublicoAlvo());
                    campanha.setLocalizacao(dadosNovos.getLocalizacao());
                    campanha.setStatus(dadosNovos.getStatus() != null ? dadosNovos.getStatus().toUpperCase() : "ATIVA");
                    campanha.setDataInicio(dadosNovos.getDataInicio());
                    campanha.setDataFim(dadosNovos.getDataFim());
                    Comunicacao atualizada = comunicacaoRepository.save(campanha);
                    return ResponseEntity.ok(atualizada);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}