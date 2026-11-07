package br.edu.ifsp.conectaavida.mobile.controller.app;

import br.edu.ifsp.conectaavida.core.domain.Usuario;
import br.edu.ifsp.conectaavida.core.repository.UsuarioCampanhaRepository;
import br.edu.ifsp.conectaavida.core.repository.UsuarioRepository;
import br.edu.ifsp.conectaavida.core.security.PasswordEncryptionService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * PERFIL DO CIDADÃO (Flutter): consultar, editar e excluir a própria conta.
 * Listar todos os usuários e criar administradores continua só na admin-api.
 */
@RestController
@RequestMapping("/api/usuarios")
public class AppUsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioCampanhaRepository usuarioCampanhaRepository;
    private final PasswordEncryptionService encryptionService;

    public AppUsuarioController(UsuarioRepository usuarioRepository,
                                UsuarioCampanhaRepository usuarioCampanhaRepository,
                                PasswordEncryptionService encryptionService) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioCampanhaRepository = usuarioCampanhaRepository;
        this.encryptionService = encryptionService;
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> buscarPorEmail(@PathVariable String email) {
        return usuarioRepository.findByEmail(email)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Usuario dados) {
        return usuarioRepository.findById(id).<ResponseEntity<?>>map(usuario -> {
            if (dados.getNome() != null) usuario.setNome(dados.getNome());
            if (dados.getLocalizacao() != null) usuario.setLocalizacao(dados.getLocalizacao());
            if (dados.getSexo() != null) usuario.setSexo(dados.getSexo());
            if (dados.getDataNascimento() != null) usuario.setDataNascimento(dados.getDataNascimento());
            if (dados.getSenha() != null && !dados.getSenha().isBlank()) {
                usuario.setSenha(encryptionService.criptografarSenha(dados.getSenha()));
            }
            // E-mail e permissão não são alterados pelo app
            return ResponseEntity.ok(usuarioRepository.save(usuario));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            usuarioCampanhaRepository.deleteByUsuarioId(id);
            usuarioRepository.deleteById(id);
            usuarioRepository.flush();
            return ResponseEntity.ok(Map.of("mensagem", "Conta excluída com sucesso."));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensagem", "Não foi possível excluir: a conta possui registros vinculados."));
        }
    }
}
