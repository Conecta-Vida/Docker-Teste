package br.edu.ifsp.conectaavida.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * ENTIDADE INTERMEDIÁRIA: USUÁRIO_CAMPANHA
 * 
 * Propósito: Tabela de junção enriquecida que rastreia o relacionamento
 * muitos-para-muitos entre Usuários e Campanhas/Comunicações com suporte
 * a auditoria, notificações de fim de campanha e exclusão em cascata.
 * 
 * Essa estrutura permite:
 * 1. Rastrear quando o usuário se inscreveu na campanha
 * 2. Saber se e quando a notificação de fim foi enviada
 * 3. Marcar como lido para campanhas urgentes
 * 4. Deletar automaticamente registros órfãos ao remover usuário ou campanha
 */
@Entity
@Table(name = "usuarios_campanhas", schema = "public")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class UsuarioCampanha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Referência ao usuário inscrito na campanha.
     * CascadeType.REMOVE e orphanRemoval = true garantem
     * que ao deletar um usuário, todos seus registros de inscrição são removidos.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false,
         foreignKey = @ForeignKey(name = "fk_usuarios_campanhas_usuario_id"))
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comunicacao_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_usuarios_campanhas_comunicacao_id"))
    private Comunicacao comunicacao;
    /**
     * Data e hora em que o usuário se inscreveu na campanha.
     * Preenchido automaticamente pelo banco via DEFAULT NOW().
     */
    @Column(name = "data_inscricao", nullable = false, insertable = false, updatable = false)
    private LocalDateTime dataInscricao;

    /**
     * Data e hora em que a notificação de fim de campanha foi enviada ao usuário.
     * NULL = ainda não foi enviada; preenchido quando o admin dispara a notificação.
     */
    @Column(name = "data_notificacao_fim")
    private LocalDateTime dataNotificacaoFim;

    /**
     * Flag que indica se o usuário já visualizou a notificação de fim.
     * Importante para comunicações urgentes (alertas, campanhas críticas).
     */
    @Column(name = "notificacao_lida", nullable = false)
    private Boolean notificacaoLida = false;

    /**
     * Timestamp de auditoria: quando este registro foi criado no banco.
     * Preenchido automaticamente via DEFAULT NOW().
     */
    @Column(name = "criado_em", nullable = false, insertable = false, updatable = false)
    private LocalDateTime criadoEm;

    /**
     * Timestamp de auditoria: última vez que este registro foi modificado.
     * Preenchido automaticamente pelo banco ao sofrer UPDATE.
     */
    @Column(name = "atualizado_em", insertable = false, updatable = false)
    private LocalDateTime atualizadoEm;
}
