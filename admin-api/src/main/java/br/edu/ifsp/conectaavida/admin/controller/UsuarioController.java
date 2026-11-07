package br.edu.ifsp.conectaavida.admin.controller;

import br.edu.ifsp.conectaavida.core.domain.Usuario;
import br.edu.ifsp.conectaavida.core.dto.UsuarioResponseDTO;
import br.edu.ifsp.conectaavida.core.mapper.UsuarioMapper;
import br.edu.ifsp.conectaavida.core.repository.UsuarioRepository;
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
}