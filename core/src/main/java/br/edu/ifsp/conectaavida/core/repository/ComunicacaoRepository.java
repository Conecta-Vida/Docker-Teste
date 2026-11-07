package br.edu.ifsp.conectaavida.core.repository;

import br.edu.ifsp.conectaavida.core.domain.Comunicacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * REPOSITÓRIO DE COMUNICAÇÕES (Notícias, Alertas e Campanhas)
 *
 * Explicação para a Equipe:
 * O JpaRepository já nos dá de brinde métodos como save(), findAll() e deleteById().
 * Nós só precisamos declarar métodos novos se quisermos filtros específicos.
 * O Spring lê o nome do método (ex: findByTipo) e escreve o "SELECT * FROM..." sozinho!
 */
@Repository
public interface ComunicacaoRepository extends JpaRepository<Comunicacao, Long> {

    // Busca todas as comunicações de um tipo específico (ex: "Alerta" ou "Notícia")
    List<Comunicacao> findByTipo(String tipo);

    // Busca filtrando por dois campos ao mesmo tempo (Tipo + Categoria)
    List<Comunicacao> findByTipoAndCategoria(String tipo, String categoria);

    // Busca filtrando por Tipo e Localização (Útil para mostrar alertas apenas de um bairro)
    List<Comunicacao> findByTipoAndLocalizacao(String tipo, String localizacao);

    // Busca pelo tipo, mas já traz ordenado do mais recente para o mais antigo pela data de início
    List<Comunicacao> findByTipoOrderByDataInicioDesc(String tipo);

    // Traz todas as comunicações de um tipo que AINDA NÃO FORAM LIDAS, ordenadas pela data de postagem
    List<Comunicacao> findByTipoAndLidoFalseOrderByDataPostadaDesc(String tipo);

    // Em vez de trazer a lista completa, apenas CONTA quantos registros existem.
    // É muito mais leve para o banco de dados e perfeito para montar o Dashboard!
    long countByTipoAndLidoFalse(String tipo);
    long countByTipoAndStatus(String tipo, String status);
    long countByTipo(String tipo);
}