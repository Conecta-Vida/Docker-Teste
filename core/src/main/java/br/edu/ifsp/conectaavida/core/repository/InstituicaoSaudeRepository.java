package br.edu.ifsp.conectaavida.core.repository;

import br.edu.ifsp.conectaavida.core.domain.InstituicaoSaude;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * REPOSITÓRIO DE INSTITUIÇÕES DE SAÚDE (Hospitais, UBS, etc.)
 */
@Repository
public interface InstituicaoSaudeRepository extends JpaRepository<InstituicaoSaude, Long> {

    /**
     * Busca a primeira instituição de saúde que encontrar baseada no tipo solicitado.
     *
     * Por que usamos Optional<>?
     * Para evitar o famoso erro "NullPointerException". Se o banco não encontrar nenhum
     * hospital com esse tipo, ele devolve um Optional vazio em vez de "null", permitindo
     * que o Controller trate o erro com elegância (ex: enviando um Erro 404 para o Frontend).
     */
    Optional<InstituicaoSaude> findTopByTipoInstituicaoOrderByIdAsc(String tipoInstituicao);
}