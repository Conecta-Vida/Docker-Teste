package br.edu.ifsp.conectaavida.repository;

import br.edu.ifsp.conectaavida.domain.LogAtividade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * INTERFACE: LogAtividadeRepository
 * Objetivo: Persistir e consultar o histórico de auditoria (quem fez o quê e quando).
 */
@Repository
public interface LogAtividadeRepository extends JpaRepository<LogAtividade, Long> {

    /**
     * Query Method de Performance (findTop5):
     * Limita o resultado do banco de dados às 5 linhas mais recentes, ordenando pela data/hora decrescente.
     * Equivalente em SQL a: SELECT * FROM logs_atividade ORDER BY data_hora DESC LIMIT 5;
     * Usado para alimentar em tempo real a timeline de atividades na página inicial do administrador.
     */
    List<LogAtividade> findTop5ByOrderByDataHoraDesc();
}