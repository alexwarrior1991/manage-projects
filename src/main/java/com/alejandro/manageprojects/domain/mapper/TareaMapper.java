package com.alejandro.manageprojects.domain.mapper;

import com.alejandro.manageprojects.domain.dto.TareaDto;
import com.alejandro.manageprojects.domain.entity.Tarea;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring", uses = {UsuarioMapper.class, ProyectoMapper.class, EtiquetaMapper.class})
public interface TareaMapper {

    @Mappings({
            @Mapping(target = "estado", expression = "java(entity.getEstado().name())")
    })
    TareaDto toDto(Tarea entity);

    @Mappings({
            @Mapping(target = "estado", expression = "java(toEstado(dto.getEstado()))")
    })
    Tarea toEntity(TareaDto dto);

    default Tarea.Estado toEstado(String estado) {
        if (estado == null) return Tarea.Estado.PENDIENTE;
        return Tarea.Estado.valueOf(estado);
    }
}
