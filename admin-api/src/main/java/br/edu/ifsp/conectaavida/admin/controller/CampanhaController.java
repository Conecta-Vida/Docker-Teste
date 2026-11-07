package br.edu.ifsp.conectaavida.admin.controller;

import br.edu.ifsp.conectaavida.admin.service.CampanhaService;
import br.edu.ifsp.conectaavida.core.domain.Comunicacao;
import br.edu.ifsp.conectaavida.core.repository.ComunicacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/campanhas")
public class CampanhaController {

    @Autowired
    private ComunicacaoRepository comunicacaoRepository;

    @Autowired
    private CampanhaService campanhaService;

    // Retorna a lista de todas as campanhas
    @GetMapping
    public ResponseEntity<List<Comunicacao>> listarCampanhas() {
        List<Comunicacao> campanhas = comunicacaoRepository.findByTipo("Campanha");
        return ResponseEntity.ok(campanhas);
    }

    // Cria uma nova campanha
    @PostMapping
    public ResponseEntity<Comunicacao> criarCampanha(@RequestBody Comunicacao campanha) {
        campanha.setTipo("Campanha");
        if (campanha.getDataPostada() == null) {
            campanha.setDataPostada(LocalDateTime.now());
        }
        Comunicacao salva = comunicacaoRepository.save(campanha);
        return ResponseEntity.ok(salva);
    }

    // Atualiza uma campanha existente (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<Comunicacao> atualizarCampanha(@PathVariable Long id, @RequestBody Comunicacao campanhaAtualizada) {
        return comunicacaoRepository.findById(id)
                .map(campanha -> {
                    campanha.setTitulo(campanhaAtualizada.getTitulo());
                    campanha.setDescricao(campanhaAtualizada.getDescricao());
                    campanha.setStatus(campanhaAtualizada.getStatus());
                    campanha.setDataInicio(campanhaAtualizada.getDataInicio());
                    campanha.setDataFim(campanhaAtualizada.getDataFim());
                    campanha.setCategoria(campanhaAtualizada.getCategoria());
                    campanha.setLocalizacao(campanhaAtualizada.getLocalizacao());
                    campanha.setPublicoAlvo(campanhaAtualizada.getPublicoAlvo());
                    Comunicacao salva = comunicacaoRepository.save(campanha);
                    return ResponseEntity.ok(salva);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Elimina uma campanha (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarCampanha(@PathVariable Long id) {
        if (comunicacaoRepository.existsById(id)) {
            comunicacaoRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Encerra uma campanha
    @PostMapping("/{id}/encerrar")
    public ResponseEntity<String> encerrarCampanha(@PathVariable Long id) {
        campanhaService.encerrarCampanha(id);
        return ResponseEntity.ok("Campanha encerrada com sucesso!");
    }
}