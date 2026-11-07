package br.edu.ifsp.conectaavida.repository;

import br.edu.ifsp.conectaavida.domain.UsuarioCampanha;
import br.edu.ifsp.conectaavida.domain.Usuario;
import br.edu.ifsp.conectaavida.domain.Comunicacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioCampanhaRepository extends JpaRepository<UsuarioCampanha, Long> {

    /**
     * Busca todas as inscrições de um usuário específico.
     */
    List<UsuarioCampanha> findByUsuarioId(Long usuarioId);

    /**
     * Busca todas as inscrições em uma campanha/comunicação específica.
     */
    List<UsuarioCampanha> findByComunicacaoId(Long comunicacaoId);

    /**
     * Verifica se um usuário está inscrito em uma campanha.
     */
    Optional<UsuarioCampanha> findByUsuarioIdAndComunicacaoId(Long usuarioId, Long comunicacaoId);

    /**
     * Busca inscrições que ainda não receberam notificação de fim de campanha.
     * Útil para disparar notificações em lote quando uma campanha termina.
     */
    @Query("SELECT uc FROM UsuarioCampanha uc " +
           "WHERE uc.comunicacao.id = :comunicacaoId " +
           "AND uc.dataNotificacaoFim IS NULL")
    List<UsuarioCampanha> findNaoNotificadasPorComunicacao(@Param("comunicacaoId") Long comunicacaoId);

    /**
     * Conta quantos usuários estão inscritos em uma campanha.
     */
    long countByComunicacaoId(Long comunicacaoId);

    /**
     * Deleta todas as inscrições de um usuário.
     * Útil ao deletar a conta do usuário.
     */
    void deleteByUsuarioId(Long usuarioId);

    /**
     * Deleta todas as inscrições em uma campanha.
     * Útil ao deletar uma campanha.
     */
    void deleteByComunicacaoId(Long comunicacaoId);
}
