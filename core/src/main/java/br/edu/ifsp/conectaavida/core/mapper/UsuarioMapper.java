package br.edu.ifsp.conectaavida.core.mapper;

import br.edu.ifsp.conectaavida.core.domain.Usuario;
import br.edu.ifsp.conectaavida.core.dto.UsuarioResponseDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {
    UsuarioResponseDTO toDto(Usuario usuario);
}