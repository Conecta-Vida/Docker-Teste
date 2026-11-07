package br.edu.ifsp.conectaavida.mobile.controller.app;

import br.edu.ifsp.conectaavida.core.domain.UsuarioCampanha;
import br.edu.ifsp.conectaavida.core.repository.ComunicacaoRepository;
import br.edu.ifsp.conectaavida.core.repository.UsuarioCampanhaRepository;
import br.edu.ifsp.conectaavida.core.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * INSCRIÇÃO DO CIDADÃO EM CAMPANHAS (vacinação, doação de sangue...).
 * Relacionamento N:M usuarios_campanhas usado pelo app Flutter.
 */
@RestController
@RequestMapping("/api/campanhas")
public class AppCampanhaController {

    private final UsuarioRepository usuarioRepository;
    private final ComunicacaoRepository comunicacaoRepository;
    private final UsuarioCampanhaRepository usuarioCampanhaRepository;

    public AppCampanhaController(UsuarioRepository usuarioRepository,
                                 ComunicacaoRepository comunicacaoRepository,
                                 UsuarioCampanhaRepository usuarioCampanhaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.comunicacaoRepository = comunicacaoRepository;
        this.usuarioCampanhaRepository = usuarioCampanhaRepository;
    }

    @PostMapping("/{comunicacaoId}/inscrever")
    @Transactional
    public ResponseEntity<?> inscrever(@PathVariable Long comunicacaoId, @RequestBody Map<String, Long> payload) {
        Long usuarioId = payload.get("usuario_id");
        if (usuarioId == null) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", "usuario_id é obrigatório."));
        }
        var usuario = usuarioRepository.findById(usuarioId).orElse(null);
        var comunicacao = comunicacaoRepository.findById(comunicacaoId).orElse(null);
        if (usuario == null || comunicacao == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensagem", "Usuário ou campanha não encontrados."));
        }
        if (usuarioCampanhaRepository.findByUsuarioIdAndComunicacaoId(usuarioId, comunicacaoId).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", "Usuário já está inscrito nesta campanha."));
        }

        UsuarioCampanha inscricao = new UsuarioCampanha();
        inscricao.setUsuario(usuario);
        inscricao.setComunicacao(comunicacao);
        UsuarioCampanha salva = usuarioCampanhaRepository.save(inscricao);
        return ResponseEntity.ok(Map.of("mensagem", "Inscrição realizada com sucesso.", "inscricao_id", salva.getId()));
    }

    @DeleteMapping("/{comunicacaoId}/desinscrever")
    @Transactional
    public ResponseEntity<?> desinscrever(@PathVariable Long comunicacaoId, @RequestBody Map<String, Long> payload) {
        Long usuarioId = payload.get("usuario_id");
        if (usuarioId == null) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", "usuario_id é obrigatório."));
        }
        return usuarioCampanhaRepository.findByUsuarioIdAndComunicacaoId(usuarioId, comunicacaoId)
                .<ResponseEntity<?>>map(inscricao -> {
                    usuarioCampanhaRepository.delete(inscricao);
                    return ResponseEntity.ok(Map.of("mensagem", "Desinscrição realizada com sucesso."));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("mensagem", "Inscrição não encontrada.")));
    }

    @GetMapping("/{comunicacaoId}/inscritos")
    @Transactional(readOnly = true)
    public ResponseEntity<?> listarInscritos(@PathVariable Long comunicacaoId) {
        List<Map<String, Object>> inscritos = new ArrayList<>();
        for (UsuarioCampanha uc : usuarioCampanhaRepository.findByComunicacaoId(comunicacaoId)) {
            if (uc.getUsuario() == null) continue;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("usuario_id", uc.getUsuario().getId());
            item.put("usuario_nome", uc.getUsuario().getNome());
            item.put("usuario_email", uc.getUsuario().getEmail());
            item.put("data_inscricao", uc.getDataInscricao() != null ? uc.getDataInscricao().toString() : "Pendente");
            item.put("data_notificacao_fim",
                    uc.getDataNotificacaoFim() != null ? uc.getDataNotificacaoFim().toString() : "Pendente");
            item.put("notificacao_lida", Boolean.TRUE.equals(uc.getNotificacaoLida()));
            inscritos.add(item);
        }
        return ResponseEntity.ok(Map.of("total_inscritos", inscritos.size(), "inscritos", inscritos));
    }
}
