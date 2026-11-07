package br.edu.ifsp.conectaavida.controller;

import br.edu.ifsp.conectaavida.domain.Comunicacao;
import br.edu.ifsp.conectaavida.domain.LogAtividade;
import br.edu.ifsp.conectaavida.domain.Usuario;
import br.edu.ifsp.conectaavida.repository.ComunicacaoRepository;
import br.edu.ifsp.conectaavida.repository.LogAtividadeRepository;
import br.edu.ifsp.conectaavida.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CONTROLLER: AlertaController
 * 🟢 PRESERVAÇÃO TOTAL: Mantida a lógica Null-Safe de atualização.
 * ⚡ ADIÇÃO DA TRILHA: Gravação automática de Log quando um alerta for arquivado ou editado.
 */
@RestController
@RequestMapping("/api/alertas")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:4000"}) // 🔥 Linha adicionada para permitir o acesso do Frontend
public class AlertaController {

    @Autowired
    private ComunicacaoRepository comunicacaoRepository;

    @Autowired
    private LogAtividadeRepository logAtividadeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/ativos")
    public ResponseEntity<List<Comunicacao>> listarAlertasAtivos() {
        List<Comunicacao> alertas = comunicacaoRepository.findByTipoAndLidoFalseOrderByDataPostadaDesc("ALERTA");
        return ResponseEntity.ok(alertas);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarAlerta(@PathVariable Long id, @RequestBody Comunicacao dadosNovos) {
        return comunicacaoRepository.findById(id)
                .map(alerta -> {
                    if (dadosNovos.getTitulo() != null) alerta.setTitulo(dadosNovos.getTitulo());
                    if (dadosNovos.getDescricao() != null) alerta.setDescricao(dadosNovos.getDescricao());
                    if (dadosNovos.getCategoria() != null) alerta.setCategoria(dadosNovos.getCategoria());
                    if (dadosNovos.getLocalizacao() != null) alerta.setLocalizacao(dadosNovos.getLocalizacao());
                    if (dadosNovos.getLido() != null) alerta.setLido(dadosNovos.getLido());

                    Comunicacao updated = comunicacaoRepository.save(alerta);

                    // 💥 REGISTRO DA TRILHA EM TEMPO REAL
                    Usuario adminLogado = usuarioRepository.findAll().stream()
                            .filter(u -> "Administrador".equalsIgnoreCase(u.getPermissao()))
                            .findFirst().orElse(null);

                    if (adminLogado != null) {
                        LogAtividade log = new LogAtividade();
                        log.setUsuario(adminLogado);
                        log.setAcao("Modificou o status/parâmetros do Alerta ID: " + id + " (Título: " + updated.getTitulo() + ")");
                        logAtividadeRepository.save(log);
                    }

                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}