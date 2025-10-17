package com.alejandro.manageprojects.domain.mapper;

import com.alejandro.manageprojects.domain.dto.UsuarioDto;
import com.alejandro.manageprojects.domain.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring", uses = {PerfilMapper.class, RolMapper.class})
public interface UsuarioMapper {

    UsuarioDto toDto(Usuario usuario);

    @Mappings({
            @Mapping(target = "tareasAsignadas", ignore = true),
            @Mapping(target = "proyectos", ignore = true),
            @Mapping(target = "creadoEn", ignore = true)
    })
    Usuario toEntity(UsuarioDto dto);
}
