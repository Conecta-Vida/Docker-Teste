package br.edu.ifsp.conectaavida.core.service;

import br.edu.ifsp.conectaavida.core.domain.Usuario;
import br.edu.ifsp.conectaavida.core.repository.UsuarioRepository;
import br.edu.ifsp.conectaavida.core.security.PasswordEncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * SERVIÇO DE USUÁRIOS
 *
 * Centraliza as regras de negócio de criação de usuários para garantir
 * que a lógica de criptografia não fique solta nos Controllers.
 */
@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncryptionService encryptionService;

    public Usuario cadastrarUsuario(Usuario usuario) {
        // Regra 1: Evitar e-mails duplicados no sistema
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("Este e-mail já está em uso no sistema.");
        }

        // Regra 2: Criptografar a senha antes de enviar para o banco
        String senhaCriptografada = encryptionService.criptografarSenha(usuario.getSenha());
        usuario.setSenha(senhaCriptografada);

        return usuarioRepository.save(usuario);
    }
}