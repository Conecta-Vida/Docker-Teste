package br.edu.ifsp.conectaavida.repository;

import br.edu.ifsp.conectaavida.domain.DispositivoPush;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DispositivoPushRepository extends JpaRepository<DispositivoPush, Long> {
    Optional<DispositivoPush> findByToken(String token);
}
