package com.alejandro.manageprojects.domain.mapper;

import com.alejandro.manageprojects.domain.dto.PresupuestoDto;
import com.alejandro.manageprojects.domain.entity.Presupuesto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

// Importante: NO depender de ProyectoMapper para evitar ciclos de beans
@Mapper(componentModel = "spring")
public interface PresupuestoMapper {

    // Evitamos mapear el back-reference al proyecto para no crear ciclos
    @Mappings({
            @Mapping(target = "proyecto", ignore = true)
    })
    PresupuestoDto toDto(Presupuesto entity);

    // Al crear/actualizar desde DTO, la asociación proyecto debe establecerse en el servicio
    @Mappings({
            @Mapping(target = "proyecto", ignore = true)
    })
    Presupuesto toEntity(PresupuestoDto dto);
}
