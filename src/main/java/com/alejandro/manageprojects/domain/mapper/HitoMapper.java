package com.alejandro.manageprojects.domain.mapper;

import com.alejandro.manageprojects.domain.dto.HitoDto;
import com.alejandro.manageprojects.domain.entity.Hito;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ProyectoMapper.class})
public interface HitoMapper {

    HitoDto toDto(Hito entity);

    Hito toEntity(HitoDto dto);
}
