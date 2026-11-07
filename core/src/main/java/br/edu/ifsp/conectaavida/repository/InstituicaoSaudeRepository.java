package br.edu.ifsp.conectaavida.repository;

import br.edu.ifsp.conectaavida.domain.InstituicaoSaude;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * INTERFACE: InstituicaoSaudeRepository
 * Objetivo: Controlar o ciclo de vida dos dados dos estabelecimentos médicos no banco.
 */
@Repository
public interface InstituicaoSaudeRepository extends JpaRepository<InstituicaoSaude, Long> {

    /**
     * Query Method com Limitador (findTop):
     * O Spring monta uma consulta que busca apenas o PRIMEIRO (LIMIT 1) registro correspondente
     * ao tipo informado, ordenando pelo ID de forma crescente.
     * * Explicação para a Equipa: Retornamos um 'Optional'. Isso obriga quem chama o método a tratar
     * a possibilidade de o banco estar vazio, blindando o sistema contra erros de tela azul (NullPointerException).
     */
    Optional<InstituicaoSaude> findTopByTipoInstituicaoOrderByIdAsc(String tipoInstituicao);
}