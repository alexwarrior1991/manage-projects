package com.alejandro.manageprojects.domain.mapper;

 import com.alejandro.manageprojects.domain.dto.HitoDto;
 import com.alejandro.manageprojects.domain.entity.Hito;
 import org.mapstruct.Mapper;
 import org.mapstruct.Mapping;
 import org.mapstruct.Mappings;

// Evitar ciclo ProyectoMapper <-> HitoMapper: este mapper NO debe depender de ProyectoMapper
@Mapper(componentModel = "spring")
 public interface HitoMapper {

    // Ignoramos el back-reference al proyecto para no requerir ProyectoMapper
    @Mappings({
            @Mapping(target = "proyecto", ignore = true)
    })
    HitoDto toDto(Hito entity);

    @Mappings({
            @Mapping(target = "proyecto", ignore = true)
    })
    Hito toEntity(HitoDto dto);
 }
