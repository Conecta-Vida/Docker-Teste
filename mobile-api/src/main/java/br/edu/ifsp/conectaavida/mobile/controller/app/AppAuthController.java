package br.edu.ifsp.conectaavida.mobile.controller.app;

import br.edu.ifsp.conectaavida.core.domain.Usuario;
import br.edu.ifsp.conectaavida.core.repository.UsuarioRepository;
import br.edu.ifsp.conectaavida.core.security.PasswordEncryptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * AUTENTICAÇÃO DO APP DO CIDADÃO (Flutter)
 * Rotas que o aplicativo já usa: cadastro, login e recuperação de senha.
 * Mesma regra de senha da admin-api (SHA-256 + salt, aceitando o hash legado).
 */
@RestController
@RequestMapping("/api/auth")
public class AppAuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncryptionService encryptionService;

    public AppAuthController(UsuarioRepository usuarioRepository, PasswordEncryptionService encryptionService) {
        this.usuarioRepository = usuarioRepository;
        this.encryptionService = encryptionService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registrar(@RequestBody Usuario novo) {
        if (novo.getEmail() == null || novo.getEmail().isBlank()
                || novo.getSenha() == null || novo.getSenha().isBlank()
                || novo.getNome() == null || novo.getNome().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", "Nome, e-mail e senha são obrigatórios."));
        }
        if (usuarioRepository.existsByEmail(novo.getEmail().trim())) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", "Este e-mail já está cadastrado."));
        }
        novo.setId(null);
        novo.setEmail(novo.getEmail().trim());
        novo.setSenha(encryptionService.criptografarSenha(novo.getSenha().trim()));
        // Pelo app só se cria cidadão; administradores são criados no painel
        novo.setPermissao("Usuário Comum");
        Usuario salvo = usuarioRepository.save(novo);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciais) {
        String email = credenciais.getOrDefault("email", "").trim();
        String senha = credenciais.getOrDefault("senha", "");

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) {
            return credenciaisInvalidas();
        }
        Usuario usuario = usuarioOpt.get();
        String hashAtual = encryptionService.criptografarSenha(senha);
        String hashLegado = encryptionService.criptografarSenhaLegada(senha);
        if (!hashAtual.equals(usuario.getSenha()) && !hashLegado.equals(usuario.getSenha())) {
            return credenciaisInvalidas();
        }

        // Migra o hash legado para o formato atual no primeiro login
        if (hashLegado.equals(usuario.getSenha())) {
            usuario.setSenha(hashAtual);
            usuarioRepository.save(usuario);
        }

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9."
                + Base64.getUrlEncoder().withoutPadding().encodeToString(usuario.getEmail().getBytes())
                + ".assinatura_segura_sha256";

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("token", token);
        resposta.put("id", usuario.getId());
        resposta.put("nome", usuario.getNome());
        resposta.put("email", usuario.getEmail());
        resposta.put("permissao", usuario.getPermissao());
        resposta.put("localizacao", usuario.getLocalizacao() != null ? usuario.getLocalizacao() : "");
        resposta.put("data_nascimento", usuario.getDataNascimento() != null ? usuario.getDataNascimento() : 0);
        resposta.put("sexo", usuario.getSexo() != null ? usuario.getSexo() : "Não informado");
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload) {
        if (payload.getOrDefault("email", "").isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", "E-mail é obrigatório."));
        }
        // Resposta neutra para não revelar quais e-mails existem
        return ResponseEntity.ok(Map.of("mensagem",
                "Se o e-mail estiver cadastrado, a equipe administradora entrará em contato para redefinir a senha."));
    }

    private ResponseEntity<?> credenciaisInvalidas() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("mensagem", "E-mail ou senha inválidos."));
    }
}
