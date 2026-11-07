package br.edu.ifsp.conectaavida.mobile.controller.app;

import br.edu.ifsp.conectaavida.core.domain.Comunicacao;
import br.edu.ifsp.conectaavida.core.domain.LogAtividade;
import br.edu.ifsp.conectaavida.core.domain.Usuario;
import br.edu.ifsp.conectaavida.core.repository.ComunicacaoRepository;
import br.edu.ifsp.conectaavida.core.repository.LogAtividadeRepository;
import br.edu.ifsp.conectaavida.core.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * FEED COMPLETO DO APP (Flutter): notícias, alertas e campanhas numa lista só,
 * no formato que o aplicativo já consome. Criar/editar/excluir fica na admin-api.
 */
@RestController
@RequestMapping("/api/comunicacoes")
public class AppComunicacaoController {

    private final ComunicacaoRepository comunicacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LogAtividadeRepository logAtividadeRepository;

    public AppComunicacaoController(ComunicacaoRepository comunicacaoRepository,
                                    UsuarioRepository usuarioRepository,
                                    LogAtividadeRepository logAtividadeRepository) {
        this.comunicacaoRepository = comunicacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.logAtividadeRepository = logAtividadeRepository;
    }

    @GetMapping
    public ResponseEntity<List<Comunicacao>> listarTodas() {
        return ResponseEntity.ok(comunicacaoRepository.findAll());
    }

    /** Registra na trilha de auditoria quando o cidadão compartilha uma notícia. */
    @PostMapping("/compartilhamentos")
    public ResponseEntity<?> registrarCompartilhamento(@RequestBody Map<String, String> payload) {
        String titulo = payload.getOrDefault("titulo", "Notícia");
        String categoria = payload.getOrDefault("categoria", "Geral");
        String sharedBy = payload.getOrDefault("sharedBy", "app_user");

        Usuario autor = usuarioRepository.findByEmail(sharedBy)
                .or(() -> usuarioRepository.findTopByPermissao("Administrador"))
                .orElse(null);
        if (autor == null) {
            // A trilha exige um usuário; sem nenhum, só confirma para o app
            return ResponseEntity.ok(Map.of("mensagem", "Compartilhamento recebido."));
        }

        LogAtividade log = new LogAtividade();
        log.setUsuarioId(autor.getId());
        log.setAcao("Compartilhamento mobile: '" + titulo + "' (" + categoria + ") por " + sharedBy);
        logAtividadeRepository.save(log);
        return ResponseEntity.ok(Map.of("mensagem", "Compartilhamento registrado com sucesso."));
    }
}
