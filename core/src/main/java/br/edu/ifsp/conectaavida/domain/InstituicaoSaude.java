package br.edu.ifsp.conectaavida.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * ENTIDADE UNIFICADA: INSTITUIÇÃO DE SAÚDE
 * Explicação para o grupo: Esta classe unifica o conceito de "OrgaoSaude" (usado no mobile)
 * com "InstituicaoSaude" (usado no painel web). Ela representa hospitais, UBSs e postos de vacinação.
 */
@Entity
@Table(name = "instituicoes_saude", schema = "public")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class InstituicaoSaude {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_instituicao", nullable = false)
    private String tipoInstituicao; // Ex: "Hospital", "Posto de Saúde", "Hemocentro"

    @Column(nullable = false, length = 150)
    private String nome;

    private String email;

    @Column(nullable = false, length = 20)
    private String telefone;

    @Column(name = "linksite")
    private String linksite; // Mapeamento explícito para snake_case do banco de dados

    private String endereco;

    @Column(name = "horario_seg_sex")
    private String horarioSegSex;

    @Column(name = "horario_sabado")
    private String horarioSabado;

    @Column(name = "horario_domingo")
    private String horarioDomingo;
}