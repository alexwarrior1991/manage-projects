package com.alejandro.manageprojects.domain.mapper;

import com.alejandro.manageprojects.domain.dto.ComentarioDto;
import com.alejandro.manageprojects.domain.entity.Comentario;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ProyectoMapper.class, UsuarioMapper.class})
public interface ComentarioMapper {

    ComentarioDto toDto(Comentario entity);

    Comentario toEntity(ComentarioDto dto);
}
