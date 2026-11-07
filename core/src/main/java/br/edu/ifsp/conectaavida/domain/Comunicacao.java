package br.edu.ifsp.conectaavida.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * ENTIDADE POLIMÓRFICA: COMUNICAÇÃO
 * Explicação para o grupo: Em vez de criar três tabelas separadas para Notícias, Alertas e Campanhas,
 * unificamos tudo em uma única tabela altamente otimizada chamada "comunicacoes".
 * O divisor de águas é o campo "tipo":
 * - Se tipo == "NOTICIA" -> Serve ao Feed de Notícias do Aplicativo Mobile.
 * - Se tipo == "CAMPANHA" -> Atende os mutirões públicos do Painel Web.
 * - Se tipo == "ALERTA" -> Dispara avisos emergenciais na tela do Gestor e Push no celular.
 */
@Entity
@Table(name = "comunicacoes", schema = "public")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Comunicacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tipo; // 'ALERTA', 'CAMPANHA' ou 'NOTICIA'

    /**
     * REQUISITO CR6: RELACIONAMENTO MUITOS-PARA-UM (UM-PARA-MUITOS INVERSO)
     * Explicação para o grupo: Muitas comunicações/notícias podem ser emitidas por um único Órgão de Saúde.
     * O campo "instituicao_id" no banco de dados faz a amarração da chave estrangeira (FK).
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "instituicao_id")
    private InstituicaoSaude instituicao;

    @Column(nullable = false)
    private String titulo;

    @Column(name = "descricao", nullable = false, columnDefinition = "TEXT")
    private String descricao; // Funde o "conteudo" da notícia mobile com a "descricao" do admin web

    private String categoria; // Ex: "Vacinação", "Surtos Epidemiológicos", "Doação de Sangue"

    @Column(name = "linkimagem")
    private String linkimagem; // Foto de capa informativa

    private String localizacao; // Região ou público geográfico alvo do aviso

    @Column(name = "publico_alvo")
    private String publicoAlvo; // Ex: "Idosos", "Gestantes", "População Geral"

    private String status; // Ex: "Ativo", "Encerrado"

    private Boolean lido = false; // Controle de leitura para alertas urgentes

    @Column(name = "data_postada", insertable = false, updatable = false)
    private LocalDateTime dataPostada; // Gravado automaticamente pelo banco via DEFAULT NOW()

    @Column(name = "data_inicio")
    private LocalDateTime dataInicio; // Início da campanha (ou data do evento da notícia do mobile)

    @Column(name = "data_fim")
    private LocalDateTime dataFim; // Fim da campanha (or data de vencimento da notícia do mobile)

    /**
     * NOVO: Coleção de inscrições de usuários nesta campanha/comunicação.
     * 
     * CascadeType.REMOVE garante que ao deletar esta comunicação,
     * todos os registros de inscrição de usuários são removidos automaticamente,
     * evitando referências órfãs no banco de dados.
     * 
     * Explicação para o grupo:
     * Este atributo mapeia o lado "um" da relação muitos-para-muitos.
     * Se uma campanha for deletada, a tabela usuarios_campanhas será limpa automaticamente.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "comunicacao", fetch = FetchType.LAZY,
               cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<UsuarioCampanha> inscritos = new HashSet<>();
}