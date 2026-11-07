package br.edu.ifsp.conectaavida.core.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * ENTIDADE: Tabela Intermediária com Dados Extras (Inscrições)
 *
 * Explicação para a Equipe:
 * Quando precisamos colocar dados extras em um relacionamento Muito-para-Muitos
 * (exemplo: saber "que dia" o usuário se inscreveu na campanha ou se ele já leu a notificação),
 * precisamos transformar a tabela intermediária em uma Entidade real no Java.
 */
@Entity
@Table(name = "usuarios_campanhas", schema = "public")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class UsuarioCampanha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Conecta com o Cidadão
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_usuarios_campanhas_usuario_id"))
    private Usuario usuario;

    // Conecta com o Mutirão/Alerta
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comunicacao_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_usuarios_campanhas_comunicacao_id"))
    private Comunicacao comunicacao;

    @Column(name = "data_inscricao", nullable = false, insertable = false, updatable = false)
    private LocalDateTime dataInscricao;

    @Column(name = "data_notificacao_fim")
    private LocalDateTime dataNotificacaoFim;

    @Column(name = "notificacao_lida", nullable = false)
    private Boolean notificacaoLida = false;

    @Column(name = "criado_em", nullable = false, insertable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", insertable = false, updatable = false)
    private LocalDateTime atualizadoEm;
}