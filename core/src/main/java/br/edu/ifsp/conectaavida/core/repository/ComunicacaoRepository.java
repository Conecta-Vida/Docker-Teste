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
// Consultas por "tipo" ignoram maiúsculas: dados antigos usam ALERTA/CAMPANHA/NOTICIA,
// o painel admin V2 grava Alerta/Campanha/Noticia.
public interface ComunicacaoRepository extends JpaRepository<Comunicacao, Long> {

    // Busca todas as comunicações de um tipo específico (ex: "Alerta" ou "Notícia")
    List<Comunicacao> findByTipoIgnoreCase(String tipo);

    // Busca filtrando por dois campos ao mesmo tempo (Tipo + Categoria)
    List<Comunicacao> findByTipoIgnoreCaseAndCategoria(String tipo, String categoria);

    // Busca filtrando por Tipo e Localização (Útil para mostrar alertas apenas de um bairro)
    List<Comunicacao> findByTipoIgnoreCaseAndLocalizacao(String tipo, String localizacao);

    // Busca pelo tipo, mas já traz ordenado do mais recente para o mais antigo pela data de início
    List<Comunicacao> findByTipoIgnoreCaseOrderByDataInicioDesc(String tipo);

    // Traz todas as comunicações de um tipo que AINDA NÃO FORAM LIDAS, ordenadas pela data de postagem
    List<Comunicacao> findByTipoIgnoreCaseAndLidoFalseOrderByDataPostadaDesc(String tipo);

    // Em vez de trazer a lista completa, apenas CONTA quantos registros existem.
    // É muito mais leve para o banco de dados e perfeito para montar o Dashboard!
    long countByTipoIgnoreCaseAndLidoFalse(String tipo);
    long countByTipoIgnoreCaseAndStatusIgnoreCase(String tipo, String status);
    long countByTipoIgnoreCase(String tipo);
}