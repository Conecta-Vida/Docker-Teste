package br.edu.ifsp.conectaavida.core.repository;

import br.edu.ifsp.conectaavida.core.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * REPOSITÓRIO DE USUÁRIOS (Cidadãos e Administradores)
 *
 * Explicação para a Equipe:
 * O coração do sistema de Login. É por aqui que o AuthController verifica
 * se quem está tentando entrar realmente existe no banco do Supabase.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Usado na hora do Login para buscar a senha criptografada do usuário
    Optional<Usuario> findByEmail(String email);

    // Usado na hora do Cadastro (Motor de Validação).
    // Devolve TRUE se o e-mail já existir no banco, ou FALSE se estiver livre.
    boolean existsByEmail(String email);

    // Busca o primeiro usuário do sistema que tenha uma permissão específica
    // (ex: Buscar se já existe pelo menos um "Administrador" na base)
    Optional<Usuario> findTopByPermissao(String permissao);
}