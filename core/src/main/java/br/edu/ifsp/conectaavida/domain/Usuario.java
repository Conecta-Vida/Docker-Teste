package br.edu.ifsp.conectaavida.domain;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios", schema = "public")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) //permite ler senha no cadastro, mas n envia pra n vazar
    @Column(nullable = false)
    private String senha;

    @Column(name = "data_nascimento")
    @JsonProperty("data_nascimento")
    private Integer dataNascimento;

    private String sexo;

    @Column(name = "localizacao")
    private String localizacao;

    @Column(name = "permissao", nullable = false)
    private String permissao;

    /**
     * REQUISITO CR7: RELACIONAMENTO MUITOS-PARA-MUITOS
     * Nota histórica: Substituído por relacionamento através de UsuarioCampanha
     * para suportar auditoria, rastreamento de notificações e exclusão em cascata.
     */
    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "usuarios_campanhas",
            schema = "public",
            joinColumns = @JoinColumn(name = "usuario_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "comunicacao_id", referencedColumnName = "id")
    )
    private Set<Comunicacao> campanhasInscritas = new HashSet<>();

    /**
     * NOVO: Relacionamento com entidade intermediária UsuarioCampanha.
     * 
     * Propósito: Permite rastreamento detalhado de cada inscrição:
     * - Quando o usuário se inscreveu (data_inscricao)
     * - Se recebeu notificação de fim (data_notificacao_fim)
     * - Se leu a notificação (notificacao_lida)
     * - Cascata automática: ao deletar usuário, todas inscrições são removidas
     * 
     * Explicação para o grupo:
     * A tabela usuarios_campanhas agora é uma "ponte inteligente" que não apenas
     * conecta dois objetos, mas também guarda informações sobre quando essa conexão
     * foi criada e o que aconteceu com essa inscrição ao longo do tempo.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY, 
               cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<UsuarioCampanha> inscricioes = new HashSet<>();
}
