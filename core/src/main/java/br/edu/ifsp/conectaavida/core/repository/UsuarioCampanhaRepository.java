package br.edu.ifsp.conectaavida.core.repository;

import br.edu.ifsp.conectaavida.core.domain.UsuarioCampanha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * REPOSITÓRIO DE INSCRIÇÕES (Tabela Intermediária: Cidadão <-> Mutirão/Campanha)
 */
@Repository
public interface UsuarioCampanhaRepository extends JpaRepository<UsuarioCampanha, Long> {

    // Lista todas as campanhas em que um cidadão específico se inscreveu
    List<UsuarioCampanha> findByUsuarioId(Long usuarioId);

    // Lista todos os cidadãos inscritos em uma campanha específica (Útil para gerar lista de presença)
    List<UsuarioCampanha> findByComunicacaoId(Long comunicacaoId);

    // Verifica se um usuário específico já está inscrito em uma campanha específica
    Optional<UsuarioCampanha> findByUsuarioIdAndComunicacaoId(Long usuarioId, Long comunicacaoId);

    /**
     * Exemplo de JPQL (Java Persistence Query Language):
     * Às vezes o nome do método ficaria gigante. Nesses casos, usamos o @Query para escrever
     * a consulta orientada a objetos. Aqui estamos buscando quem está na campanha X,
     * mas que AINDA NÃO recebeu a notificação de encerramento.
     */
    @Query("SELECT uc FROM UsuarioCampanha uc WHERE uc.comunicacao.id = :comunicacaoId AND uc.dataNotificacaoFim IS NULL")
    List<UsuarioCampanha> findNaoNotificadasPorComunicacao(@Param("comunicacaoId") Long comunicacaoId);

    // Conta quantas pessoas estão em um mutirão de saúde
    long countByComunicacaoId(Long comunicacaoId);

    // Apaga todas as inscrições de um usuário (Usado caso o cidadão exclua a conta dele)
    void deleteByUsuarioId(Long usuarioId);

    // Apaga todas as inscrições atreladas a uma campanha (Usado caso o administrador cancele o evento)
    void deleteByComunicacaoId(Long comunicacaoId);
}