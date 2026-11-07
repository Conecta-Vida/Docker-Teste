package br.edu.ifsp.conectaavida.repository;

import br.edu.ifsp.conectaavida.domain.Comunicacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * REPOSITÓRIO UNIFICADO: COMUNICAÇÃO (ALERTAS, CAMPANHAS E NOTÍCIAS)
 * * Explicação para o grupo (Luiz, Gustavo, Gabriel, Renan e Maycon):
 * Esta interface utiliza um recurso do Spring Data JPA chamado "Query Methods" (Métodos de Consulta).
 * Nós não precisamos escrever códigos SQL na mão aqui! O Spring lê o nome que damos ao método
 * (desde que siga as palavras-chave como 'And', 'False', 'OrderBy', 'Count') e monta a consulta
 * no banco do Supabase de forma 100% automática nos bastidores.
 */
@Repository
public interface ComunicacaoRepository extends JpaRepository<Comunicacao, Long> {

    // ===================================================================
    // METODOS DE BUSCA COMPARTILHADOS (WEB ADMIN & MOBILE)
    // ===================================================================

    // SQL gerado: SELECT * FROM comunicacoes WHERE tipo = ?;
    List<Comunicacao> findByTipo(String tipo);

    // SQL gerado: SELECT * FROM comunicacoes WHERE tipo = ? AND categoria = ?;
    List<Comunicacao> findByTipoAndCategoria(String tipo, String categoria);

    // SQL gerado: SELECT * FROM comunicacoes WHERE tipo = ? AND localizacao = ?;
    List<Comunicacao> findByTipoAndLocalizacao(String tipo, String localizacao);

    // Adicione esta linha dentro do escopo da sua interface ComunicacaoRepository:
    List<Comunicacao> findByTipoOrderByDataInicioDesc(String tipo);

    // ===================================================================
    // SOLUÇÃO DOS ERROS: MÉTODOS EXIGIDOS PELOS CONTROLADORES
    // ===================================================================

    /**
     * SOLUÇÃO DO ERRO 1 (Exigido pelo AlertaController):
     * Filtra os alertas emergenciais que ainda NÃO foram lidos (lido == false)
     * e os entrega ordenados da data mais recente para a mais antiga.
     * * SQL Automático:
     * SELECT * FROM public.comunicacoes WHERE tipo = ? AND lido = false ORDER BY data_postada DESC;
     */
    List<Comunicacao> findByTipoAndLidoFalseOrderByDataPostadaDesc(String tipo);

    /**
     * SOLUÇÃO DO ERRO 2 (Exigido pelo DashboardController):
     * Faz a contagem de quantos alertas emergenciais ativos (não lidos) existem no sistema
     * para renderizar o número vermelho no indicador do Painel Web.
     * * SQL Automático:
     * SELECT COUNT(*) FROM public.comunicacoes WHERE tipo = ? AND lido = false;
     */
    long countByTipoAndLidoFalse(String tipo);

    /**
     * SOLUÇÃO DO ERRO 3 (Exigido pelo DashboardController):
     * Faz a contagem de quantas campanhas (mutirões) estão cadastradas com um status específico (Ex: "Ativa").
     * Alimentará o card de métricas de engajamento da comunidade.
     * * SQL Automático:
     * SELECT COUNT(*) FROM public.comunicacoes WHERE tipo = ? AND status = ?;
     */
    long countByTipoAndStatus(String tipo, String status);

    /**
     * SOLUÇÃO DO ERRO 4 (Exigido pelo DashboardController):
     * Conta o volume bruto de publicações de um determinado tipo (Ex: tipo "NOTICIA")
     * cadastradas no sistema para exibir no gráfico consolidado.
     * * SQL Automático:
     * SELECT COUNT(*) FROM public.comunicacoes WHERE tipo = ?;
     */
    long countByTipo(String tipo);
}