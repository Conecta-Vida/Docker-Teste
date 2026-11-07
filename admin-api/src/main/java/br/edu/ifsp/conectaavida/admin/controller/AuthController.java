package br.edu.ifsp.conectaavida.admin.controller;

import br.edu.ifsp.conectaavida.core.domain.Usuario;
import br.edu.ifsp.conectaavida.core.repository.UsuarioRepository;
import br.edu.ifsp.conectaavida.core.security.PasswordEncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * CONTROLLER DE AUTENTICAÇÃO
 *
 * Explicação para a Equipe:
 * O @RestController avisa ao Spring que esta classe vai devolver dados em formato JSON.
 * O @RequestMapping("/api/auth") define a URL base. Todas as rotas aqui começam com isso.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncryptionService encryptionService;

    /**
     * Rota de Login do Painel Administrativo.
     * Recebe e-mail e senha, criptografa a senha digitada e compara com a do banco.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciais) {
        String email = credenciais.get("email");
        String senha = credenciais.get("senha");

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            String senhaCriptografada = encryptionService.criptografarSenha(senha);

            // Verifica se a senha bate e se a pessoa tem permissão de Administrador
            if (usuario.getSenha().equals(senhaCriptografada)) {
                if (!"Administrador".equalsIgnoreCase(usuario.getPermissao())) {
                    // Erro 403 Forbidden: Acesso Negado
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("erro", "Acesso restrito apenas para Gestores."));
                }
                // Login com Sucesso! Devolve os dados do usuário pro Painel React.
                return ResponseEntity.ok(usuario);
            }
        }
        // Erro 401 Unauthorized: Senha errada ou e-mail não existe
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("erro", "Credenciais inválidas."));
    }
}