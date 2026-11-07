package br.edu.ifsp.conectaavida.core.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UsuarioResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private String permissao;
}