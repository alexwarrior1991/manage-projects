package com.alejandro.manageprojects.domain.mapper;

import com.alejandro.manageprojects.domain.dto.ProyectoDto;
import com.alejandro.manageprojects.domain.entity.Proyecto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring", uses = {PresupuestoMapper.class, TareaMapper.class, ComentarioMapper.class, HitoMapper.class})
public interface ProyectoMapper {

    ProyectoDto toDto(Proyecto entity);

    @Mappings({
            @Mapping(target = "miembros", ignore = true)
    })
    Proyecto toEntity(ProyectoDto dto);
}
