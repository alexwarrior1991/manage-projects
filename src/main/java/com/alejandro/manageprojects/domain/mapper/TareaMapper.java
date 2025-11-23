package com.alejandro.manageprojects.domain.mapper;

import com.alejandro.manageprojects.domain.dto.TareaDto;
import com.alejandro.manageprojects.domain.entity.Tarea;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

// Importante: evitar ciclo ProyectoMapper <-> TareaMapper. Este mapper NO debe depender de ProyectoMapper
@Mapper(componentModel = "spring", uses = {UsuarioMapper.class, EtiquetaMapper.class})
public interface TareaMapper {

    @Mappings({
            @Mapping(target = "estado", expression = "java(entity.getEstado().name())"),
            // Evitar back-reference para no requerir ProyectoMapper
            @Mapping(target = "proyecto", ignore = true)
    })
    TareaDto toDto(Tarea entity);

    @Mappings({
            @Mapping(target = "estado", expression = "java(toEstado(dto.getEstado()))"),
            // La asociación se establecerá en la capa de servicio si es necesaria
            @Mapping(target = "proyecto", ignore = true)
    })
    Tarea toEntity(TareaDto dto);

    default Tarea.Estado toEstado(String estado) {
        if (estado == null) return Tarea.Estado.PENDIENTE;
        return Tarea.Estado.valueOf(estado);
    }
}
