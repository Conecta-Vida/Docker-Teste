package br.edu.ifsp.conectaavida.core.repository;

import br.edu.ifsp.conectaavida.core.domain.LogAtividade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogAtividadeRepository extends JpaRepository<LogAtividade, Long> {
    // Retorna os últimos 10 logs ordenados por data decrescente
    List<LogAtividade> findTop10ByOrderByDataHoraDesc();
}