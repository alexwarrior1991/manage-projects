package com.alejandro.manageprojects.domain.mapper;

import com.alejandro.manageprojects.domain.dto.EtiquetaDto;
import com.alejandro.manageprojects.domain.entity.Etiqueta;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EtiquetaMapper {
    EtiquetaDto toDto(Etiqueta entity);
    Etiqueta toEntity(EtiquetaDto dto);
}
