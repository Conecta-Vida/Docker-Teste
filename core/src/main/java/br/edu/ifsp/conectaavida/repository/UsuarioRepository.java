package br.edu.ifsp.conectaavida.repository;

import br.edu.ifsp.conectaavida.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // O Spring Data gera automaticamente: SELECT * FROM usuarios WHERE email = ?
    Optional<Usuario> findByEmail(String email);

    // Verifica duplicidade de contas no cadastro (Se retornar true, barra o insert)
    boolean existsByEmail(String email);

    // Busca o primeiro administrador que encontrar no banco de forma otimizada!
    Optional<Usuario> findTopByPermissao(String permissao);
}