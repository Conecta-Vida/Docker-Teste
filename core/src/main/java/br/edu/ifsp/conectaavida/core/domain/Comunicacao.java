package br.edu.ifsp.conectaavida.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * ENTIDADE: Comunicações (Notícias, Alertas e Campanhas)
 *
 * Explicação para a Equipe:
 * O @Entity avisa ao Spring que esta classe representa uma tabela no banco de dados.
 * O Lombok (@Getter, @Setter) gera os métodos get/set automaticamente nos bastidores, deixando o código limpo.
 */
@Entity
@Table(name = "comunicacoes", schema = "public") // Define o nome exato da tabela no PostgreSQL
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Comunicacao {

    @Id // Define que este campo é a Chave Primária (Primary Key)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // O banco de dados vai gerar o ID automaticamente (Auto-Incremento)
    private Long id;

    @Column(nullable = false) // Garante que o banco não aceite salvar sem preencher o tipo
    private String tipo;

    /**
     * RELACIONAMENTO: Muitas Comunicações pertencem a Uma Instituição de Saúde.
     * O FetchType.EAGER faz com que, ao carregar a notícia, o Spring já traga
     * os dados do hospital junto automaticamente.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "instituicao_id")
    private InstituicaoSaude instituicao;

    @Column(nullable = false)
    private String titulo;

    // columnDefinition = "TEXT" permite que o texto seja gigante (ideal para o corpo de notícias)
    @Column(name = "descricao", nullable = false, columnDefinition = "TEXT")
    private String descricao;

    private String categoria;

    @Column(name = "linkimagem")
    private String linkimagem;

    private String localizacao;

    @Column(name = "publico_alvo")
    private String publicoAlvo;

    private String status;

    private Boolean lido = false;

    // updatable = false garante que a data de postagem nunca seja alterada depois de criada
    @Column(name = "data_postada", insertable = false, updatable = false)
    private LocalDateTime dataPostada;

    @Column(name = "data_inicio")
    private LocalDateTime dataInicio;

    @Column(name = "data_fim")
    private LocalDateTime dataFim;

    /**
     * RELACIONAMENTO INVERSO: Uma Comunicação tem Muitos inscritos (Mutirão de Saúde).
     * O @JsonIgnore é VITAL aqui! Ele impede que o Spring tente enviar a lista infinita de cidadãos
     * no JSON quando o Flutter pedir a lista de Campanhas, evitando travar o servidor.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "comunicacao", fetch = FetchType.LAZY,
            cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<UsuarioCampanha> inscritos = new HashSet<>();
}