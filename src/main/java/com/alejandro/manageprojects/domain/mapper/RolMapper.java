package com.alejandro.manageprojects.domain.mapper;

import com.alejandro.manageprojects.domain.dto.RolDto;
import com.alejandro.manageprojects.domain.entity.Rol;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RolMapper {
    RolDto toDto(Rol entity);
    Rol toEntity(RolDto dto);
}
