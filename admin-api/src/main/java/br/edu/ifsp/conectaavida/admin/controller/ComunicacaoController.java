package br.edu.ifsp.conectaavida.admin.controller;

import br.edu.ifsp.conectaavida.core.domain.Comunicacao;
import br.edu.ifsp.conectaavida.core.repository.ComunicacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/comunicacoes")
public class ComunicacaoController {

    @Autowired
    private ComunicacaoRepository comunicacaoRepository;

    @GetMapping
    public ResponseEntity<List<Comunicacao>> listarTodas() {
        List<Comunicacao> comunicacoes = comunicacaoRepository.findAll();
        return ResponseEntity.ok(comunicacoes);
    }

    @PostMapping
    public ResponseEntity<Comunicacao> criar(@RequestBody Comunicacao comunicacao) {
        if (comunicacao.getDataPostada() == null) {
            comunicacao.setDataPostada(LocalDateTime.now());
        }
        Comunicacao salva = comunicacaoRepository.save(comunicacao);
        return ResponseEntity.ok(salva);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Comunicacao> atualizar(@PathVariable Long id, @RequestBody Comunicacao atualizada) {
        return comunicacaoRepository.findById(id)
                .map(comunicacao -> {
                    comunicacao.setTipo(atualizada.getTipo());
                    comunicacao.setTitulo(atualizada.getTitulo());
                    comunicacao.setDescricao(atualizada.getDescricao());
                    comunicacao.setCategoria(atualizada.getCategoria());
                    comunicacao.setLinkimagem(atualizada.getLinkimagem());
                    comunicacao.setLocalizacao(atualizada.getLocalizacao());
                    comunicacao.setPublicoAlvo(atualizada.getPublicoAlvo());
                    comunicacao.setStatus(atualizada.getStatus());
                    comunicacao.setDataInicio(atualizada.getDataInicio());
                    comunicacao.setDataFim(atualizada.getDataFim());
                    Comunicacao salva = comunicacaoRepository.save(comunicacao);
                    return ResponseEntity.ok(salva);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (comunicacaoRepository.existsById(id)) {
            comunicacaoRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}