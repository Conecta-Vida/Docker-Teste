package br.edu.ifsp.conectaavida.admin.controller;

import br.edu.ifsp.conectaavida.core.domain.Usuario;
import br.edu.ifsp.conectaavida.core.dto.UsuarioResponseDTO;
import br.edu.ifsp.conectaavida.core.mapper.UsuarioMapper;
import br.edu.ifsp.conectaavida.core.repository.UsuarioRepository;
import br.edu.ifsp.conectaavida.core.security.PasswordEncryptionService;
import br.edu.ifsp.conectaavida.core.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CONTROLLER DE USUÁRIOS
 *
 * Explicação para a Equipe:
 * Gerencia a lista de usuários no painel. Agora utiliza o UsuarioMapper (MapStruct)
 * na rota GET para entregar ao React apenas o DTO limpo, protegendo dados sensíveis.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    // Injeção do nosso novo conversor automático
    @Autowired
    private UsuarioMapper usuarioMapper;

    @Autowired
    private PasswordEncryptionService encryptionService;

    // Lista todos os usuários do sistema retornando apenas dados seguros (DTO)
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        List<UsuarioResponseDTO> usuariosSeguros = usuarioRepository.findAll()
                .stream()
                .map(usuarioMapper::toDto)
                .toList();
        return ResponseEntity.ok(usuariosSeguros);
    }

    // Cria um novo usuário pelo painel admin (ex: adicionando um novo enfermeiro)
    @PostMapping
    public ResponseEntity<?> criarUsuario(@RequestBody Usuario usuario) {
        try {
            // Passa a "batata quente" para o nosso Serviço validar e salvar
            Usuario novoUsuario = usuarioService.cadastrarUsuario(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(novoUsuario);
        } catch (IllegalArgumentException e) {
            // Se o e-mail já existir, o Serviço lança um erro e nós devolvemos HTTP 400 Bad Request
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarUsuario(@PathVariable Long id) {
        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        usuarioRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Usado pelo painel admin V2 (botão "Editar"). Só altera os campos enviados.
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarUsuario(@PathVariable Long id, @RequestBody Usuario dados) {
        return usuarioRepository.findById(id).map(usuario -> {
            if (dados.getNome() != null) usuario.setNome(dados.getNome());
            if (dados.getEmail() != null) usuario.setEmail(dados.getEmail());
            if (dados.getPermissao() != null) usuario.setPermissao(dados.getPermissao());
            if (dados.getLocalizacao() != null) usuario.setLocalizacao(dados.getLocalizacao());
            if (dados.getSexo() != null) usuario.setSexo(dados.getSexo());
            if (dados.getDataNascimento() != null) usuario.setDataNascimento(dados.getDataNascimento());
            if (dados.getSenha() != null && !dados.getSenha().isBlank()) {
                usuario.setSenha(encryptionService.criptografarSenha(dados.getSenha()));
            }
            return ResponseEntity.ok(usuarioMapper.toDto(usuarioRepository.save(usuario)));
        }).orElse(ResponseEntity.notFound().build());
    }
}
