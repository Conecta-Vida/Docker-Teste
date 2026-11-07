package br.edu.ifsp.conectaavida.admin.controller;

import br.edu.ifsp.conectaavida.core.domain.Comunicacao;
import br.edu.ifsp.conectaavida.core.repository.ComunicacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    @Autowired
    private ComunicacaoRepository comunicacaoRepository;

    // Retorna a lista de todos os alertas
    @GetMapping
    public ResponseEntity<List<Comunicacao>> listarAlertas() {
        List<Comunicacao> alertas = comunicacaoRepository.findByTipo("Alerta");
        return ResponseEntity.ok(alertas);
    }

    // Cria um novo alerta
    @PostMapping
    public ResponseEntity<Comunicacao> criarAlerta(@RequestBody Comunicacao alerta) {
        alerta.setTipo("Alerta");
        if (alerta.getDataPostada() == null) {
            alerta.setDataPostada(LocalDateTime.now());
        }
        Comunicacao salva = comunicacaoRepository.save(alerta);
        return ResponseEntity.ok(salva);
    }

    // Atualiza um alerta existente (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<Comunicacao> atualizarAlerta(@PathVariable Long id, @RequestBody Comunicacao alertaAtualizado) {
        return comunicacaoRepository.findById(id)
                .map(alerta -> {
                    alerta.setTitulo(alertaAtualizado.getTitulo());
                    alerta.setDescricao(alertaAtualizado.getDescricao());
                    alerta.setStatus(alertaAtualizado.getStatus());
                    alerta.setDataInicio(alertaAtualizado.getDataInicio());
                    alerta.setDataFim(alertaAtualizado.getDataFim());
                    alerta.setCategoria(alertaAtualizado.getCategoria());
                    alerta.setLocalizacao(alertaAtualizado.getLocalizacao());
                    alerta.setPublicoAlvo(alertaAtualizado.getPublicoAlvo());
                    Comunicacao salva = comunicacaoRepository.save(alerta);
                    return ResponseEntity.ok(salva);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Elimina um alerta (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarAlerta(@PathVariable Long id) {
        if (comunicacaoRepository.existsById(id)) {
            comunicacaoRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}