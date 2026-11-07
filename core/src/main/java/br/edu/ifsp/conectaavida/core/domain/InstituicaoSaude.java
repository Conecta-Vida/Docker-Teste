package br.edu.ifsp.conectaavida.core.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * ENTIDADE: Instituições de Saúde (Hospitais, Postos de Saúde, UBS)
 *
 * Explicação para a Equipe:
 * Esta classe armazena os dados físicos dos locais. O campo "length = 150"
 * otimiza o banco de dados limitando o tamanho da String (VARCHAR).
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
    private String tipoInstituicao;

    @Column(nullable = false, length = 150)
    private String nome;

    private String email;

    @Column(nullable = false, length = 20)
    private String telefone;

    @Column(name = "linksite")
    private String linksite;

    private String endereco;

    @Column(name = "horario_seg_sex")
    private String horarioSegSex;

    @Column(name = "horario_sabado")
    private String horarioSabado;

    @Column(name = "horario_domingo")
    private String horarioDomingo;
}