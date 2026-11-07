package br.edu.ifsp.conectaavida.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * ENTIDADE: LOG DE ATIVIDADE
 * Explicação para o grupo: Atende o requisito de auditoria do sistema.
 * Grava na trilha de auditoria quem fez o quê e em qual horário no painel administrativo.
 */
@Entity
@Table(name = "logs_atividade", schema = "public")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class LogAtividade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario; // O administrador responsável pela ação

    @Column(nullable = false)
    private String acao; // Descrição da ação (Ex: "Publicou Alerta de Dengue", "Importou Cidadãos via CSV")

    @Column(name = "data_hora", nullable = false, insertable = false, updatable = false)
    private LocalDateTime dataHora;
}