package com.alejandro.manageprojects.domain.mapper;

import com.alejandro.manageprojects.domain.dto.ComentarioDto;
import com.alejandro.manageprojects.domain.entity.Comentario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

// Evitar ciclo bidireccional ProyectoMapper <-> ComentarioMapper: no dependemos de ProyectoMapper
@Mapper(componentModel = "spring", uses = {UsuarioMapper.class})
public interface ComentarioMapper {

    // Ignoramos el back-reference al proyecto; se gestiona desde servicio si es necesario
    @Mappings({
            @Mapping(target = "proyecto", ignore = true)
    })
    ComentarioDto toDto(Comentario entity);

    @Mappings({
            @Mapping(target = "proyecto", ignore = true)
    })
    Comentario toEntity(ComentarioDto dto);
}
