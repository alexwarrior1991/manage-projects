package com.alejandro.manageprojects.domain.mapper;

import com.alejandro.manageprojects.domain.dto.PresupuestoDto;
import com.alejandro.manageprojects.domain.entity.Presupuesto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ProyectoMapper.class})
public interface PresupuestoMapper {

    PresupuestoDto toDto(Presupuesto entity);

    Presupuesto toEntity(PresupuestoDto dto);
}
