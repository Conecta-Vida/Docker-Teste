package br.edu.ifsp.conectaavida.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

/**
 * ENTIDADE: Usuários (Cidadãos Comuns e Administradores)
 *
 * Explicação para a Equipe:
 * Esta classe representa qualquer pessoa que faz login no sistema (seja no Flutter ou no Web).
 * É ela que guarda a chave de permissão que o Mobile usa para liberar o Modo Gestor[cite: 2].
 */
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

    /**
     * SEGURANÇA MÁXIMA:
     * O "WRITE_ONLY" impede que a senha (mesmo criptografada) vaze em respostas JSON.
     * O sistema só permite "escrever" a senha no login/cadastro, mas nunca "lê" ela de volta pra tela.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String senha;

    // O @JsonProperty força o JSON a usar o formato snake_case ("data_nascimento") esperado pelo Front-end
    @Column(name = "data_nascimento")
    @JsonProperty("data_nascimento")
    private Integer dataNascimento;

    private String sexo;

    @Column(name = "localizacao")
    private String localizacao;

    @Column(name = "permissao", nullable = false)
    private String permissao;

    /**
     * RELACIONAMENTO N:N (Muitos para Muitos)
     * Um usuário pode estar em várias campanhas, e uma campanha tem vários usuários.
     * O Spring cria automaticamente a tabela invisível "usuarios_campanhas" para conectar os dois.
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

    @JsonIgnore
    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY,
            cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<UsuarioCampanha> inscricioes = new HashSet<>();
}