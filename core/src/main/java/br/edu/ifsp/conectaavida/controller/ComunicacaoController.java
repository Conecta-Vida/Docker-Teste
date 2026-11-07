package br.edu.ifsp.conectaavida.controller;

import br.edu.ifsp.conectaavida.mensageria.EventoPublisher;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import br.edu.ifsp.conectaavida.domain.Comunicacao;
import br.edu.ifsp.conectaavida.domain.LogAtividade;
import br.edu.ifsp.conectaavida.domain.Usuario;
import br.edu.ifsp.conectaavida.repository.ComunicacaoRepository;
import br.edu.ifsp.conectaavida.repository.LogAtividadeRepository;
import br.edu.ifsp.conectaavida.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * CONTROLLER: ComunicacaoController
 * TRILHA: Grava logs automáticos de postagens e deleções de informativos.
 */
@RestController
@RequestMapping("/api/comunicacoes")
public class ComunicacaoController {

    @Autowired
    private ComunicacaoRepository comunicacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LogAtividadeRepository logAtividadeRepository;

    @Autowired
    private EventoPublisher eventoPublisher;

    @PostMapping
    public ResponseEntity<?> cadastrarComunicacao(@RequestBody Comunicacao comunicacao) {
        try {
            comunicacao.setDataPostada(LocalDateTime.now());
            // Sem "lido" no JSON o valor chegava null e o alerta sumia da lista de ativos
            if (comunicacao.getLido() == null) {
                comunicacao.setLido(false);
            }
            Comunicacao salva = comunicacaoRepository.save(comunicacao);

            // ALERTA vira evento no RabbitMQ -> mobile-api dispara o push
            if ("ALERTA".equalsIgnoreCase(salva.getTipo())) {
                eventoPublisher.publicarAlerta(salva);
            }

            return ResponseEntity.ok(salva);
        } catch (IllegalArgumentException e) {
            // Se for um erro de validação retorna 400 Bad Request
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            // Se for erro na nuvem/banco retorna 500 Internal Error
            return ResponseEntity.internalServerError().body(Map.of("message", e.getLocalizedMessage()));
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarComunicacao(@PathVariable Long id) {
        return comunicacaoRepository.findById(id).map(comunicacao -> {

            // Captura o admin antes de deletar a comunicação
            Usuario adminLogado = usuarioRepository.findAll().stream()
                    .filter(u -> "Administrador".equalsIgnoreCase(u.getPermissao()))
                    .findFirst().orElse(null);

            // Desvincula usuários do Flutter inscritos (tabela associativa)
            usuarioRepository.findAll().forEach(usuario -> {
                if (usuario.getCampanhasInscritas() != null) {
                    if (usuario.getCampanhasInscritas().removeIf(c -> c.getId().equals(id))) {
                        usuarioRepository.save(usuario);
                    }
                }
            });

            comunicacaoRepository.delete(comunicacao);

            // REGISTRO DA TRILHA EM TEMPO REAL
            if (adminLogado != null) {
                LogAtividade log = new LogAtividade();
                log.setUsuario(adminLogado);
                log.setAcao("Excluiu permanentemente a publicação/campanha: " + comunicacao.getTitulo());
                logAtividadeRepository.save(log);
            }

            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarComunicacao(@PathVariable Long id, @RequestBody Comunicacao dadosNovos) {
        return comunicacaoRepository.findById(id).map(comunicacao -> {
            // Atualiza apenas os dados que vieram na requisição
            if (dadosNovos.getTitulo() != null) comunicacao.setTitulo(dadosNovos.getTitulo());
            if (dadosNovos.getDescricao() != null) comunicacao.setDescricao(dadosNovos.getDescricao());
            if (dadosNovos.getCategoria() != null) comunicacao.setCategoria(dadosNovos.getCategoria());
            if (dadosNovos.getLocalizacao() != null) comunicacao.setLocalizacao(dadosNovos.getLocalizacao());
            
            Comunicacao atualizada = comunicacaoRepository.save(comunicacao);

            // REGISTRO DA TRILHA
            Usuario adminLogado = usuarioRepository.findTopByPermissao("Administrador").orElse(null);
            if (adminLogado != null) {
                LogAtividade log = new LogAtividade();
                log.setUsuario(adminLogado);
                log.setAcao("Editou a comunicação ID: " + id + " (" + atualizada.getTitulo() + ")");
                logAtividadeRepository.save(log);
            }

            return ResponseEntity.ok(atualizada);
        }).orElse(ResponseEntity.notFound().build());
    }

    
    @GetMapping
    public ResponseEntity<List<Comunicacao>> listarTodas() {
        return ResponseEntity.ok(comunicacaoRepository.findAll());
    }

    @PostMapping("/compartilhamentos")
    public ResponseEntity<?> registrarCompartilhamento(@RequestBody Map<String, String> payload) {
        try {
            String titulo = payload.getOrDefault("titulo", "Notícia");
            String categoria = payload.getOrDefault("categoria", "Geral");
            String sharedBy = payload.getOrDefault("sharedBy", "app_user");

            Usuario usuarioRef = usuarioRepository.findByEmail(sharedBy)
                    .or(() -> usuarioRepository.findTopByPermissao("Administrador"))
                    .orElse(null);

            LogAtividade log = new LogAtividade();
            log.setUsuario(usuarioRef);
            log.setAcao("Compartilhamento mobile: '" + titulo + "' (" + categoria + ") por " + sharedBy);
            logAtividadeRepository.save(log);

            return ResponseEntity.ok(Map.of("mensagem", "Compartilhamento registrado com sucesso."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("mensagem", "Falha ao registrar compartilhamento."));
        }
    }
}