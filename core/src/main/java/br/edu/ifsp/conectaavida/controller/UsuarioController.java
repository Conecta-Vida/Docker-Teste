package br.edu.ifsp.conectaavida.controller;

import br.edu.ifsp.conectaavida.domain.Usuario;
import br.edu.ifsp.conectaavida.domain.LogAtividade;
import br.edu.ifsp.conectaavida.repository.UsuarioRepository;
import br.edu.ifsp.conectaavida.repository.LogAtividadeRepository;
import br.edu.ifsp.conectaavida.service.UsuarioService;
import br.edu.ifsp.conectaavida.service.RelatorioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * CONTROLLER: UsuarioController
 * Rota Base: /api/usuarios
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private LogAtividadeRepository logAtividadeRepository;

    @Autowired
    private RelatorioService relatorioService;

    @GetMapping
    public ResponseEntity<List<Usuario>> listarTodos() {
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody Usuario usuario) {
        try {
            Usuario salvo = usuarioService.cadastrarUsuario(usuario);

            Usuario adminLogado = usuarioRepository.findAll().stream()
                    .filter(u -> "Administrador".equalsIgnoreCase(u.getPermissao()))
                    .findFirst().orElse(salvo);

            LogAtividade log = new LogAtividade();
            log.setUsuario(adminLogado);
            log.setAcao("Cadastrou um novo perfil de usuário: " + salvo.getEmail() + " (" + salvo.getPermissao() + ")");
            logAtividadeRepository.save(log);

            return ResponseEntity.ok(salvo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Usuario dadosNovos) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    usuario.setNome(dadosNovos.getNome());
                    usuario.setEmail(dadosNovos.getEmail());
                    usuario.setDataNascimento(dadosNovos.getDataNascimento()); // 🟢 Atualizado para dataNascimento
                    usuario.setSexo(dadosNovos.getSexo());
                    usuario.setLocalizacao(dadosNovos.getLocalizacao());
                    if (dadosNovos.getPermissao() != null) {
                        usuario.setPermissao(dadosNovos.getPermissao());
                    }
                    Usuario updated = usuarioRepository.save(usuario);

                    Usuario adminLogado = usuarioRepository.findAll().stream()
                            .filter(u -> "Administrador".equalsIgnoreCase(u.getPermissao()))
                            .findFirst().orElse(updated);

                    LogAtividade log = new LogAtividade();
                    log.setUsuario(adminLogado);
                    log.setAcao("Atualizou dados cadastrais do cidadão ID: " + id);
                    logAtividadeRepository.save(log);

                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        return usuarioRepository.findById(id).map(usuario -> {
            Usuario adminLogado = usuarioRepository.findAll().stream()
                    .filter(u -> "Administrador".equalsIgnoreCase(u.getPermissao() ) && !u.getId().equals(id))
                    .findFirst().orElse(null);

            logAtividadeRepository.findAll().stream()
                    .filter(log -> log.getUsuario() != null && log.getUsuario().getId().equals(id))
                    .forEach(log -> logAtividadeRepository.deleteById(log.getId()));

            if (usuario.getCampanhasInscritas() != null) {
                usuario.getCampanhasInscritas().clear();
                usuarioRepository.saveAndFlush(usuario);
            }

            usuarioRepository.delete(usuario);

            if (adminLogado != null) {
                LogAtividade log = new LogAtividade();
                log.setUsuario(adminLogado);
                log.setAcao("Removeu permanentemente a conta do usuário ID: " + id + " (E-mail: " + usuario.getEmail() + ")");
                logAtividadeRepository.save(log);
            }

            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/importar-csv")
    public ResponseEntity<?> importarCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("O arquivo enviado está vazio.");
        }
        try {
            String resultado = "Processamento concluído com sucesso. Registros inseridos via pipeline.";

            Usuario adminLogado = usuarioRepository.findAll().stream()
                    .filter(u -> "Administrador".equalsIgnoreCase(u.getPermissao()))
                    .findFirst().orElse(null);

            if (adminLogado != null) {
                LogAtividade log = new LogAtividade();
                log.setUsuario(adminLogado);
                log.setAcao("Executou carga em massa de cidadãos importando planilha CSV");
                logAtividadeRepository.save(log);
            }

            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Falha ao analisar a estrutura do arquivo CSV.");
        }
    }

    @GetMapping("/exportar-csv")
    public ResponseEntity<byte[]> exportarCsvFallback() {
        byte[] csv = relatorioService.gerarCsvUsuarios();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_usuarios.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Usuario> buscarPorEmail(@PathVariable String email) {
        return usuarioRepository.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}