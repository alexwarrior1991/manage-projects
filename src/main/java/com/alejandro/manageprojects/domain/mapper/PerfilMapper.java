package com.alejandro.manageprojects.domain.mapper;

import com.alejandro.manageprojects.domain.dto.PerfilDto;
import com.alejandro.manageprojects.domain.entity.Perfil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface PerfilMapper {

    // Evitamos dependencia circular con UsuarioMapper ignorando el back-reference
    @Mappings({
            @Mapping(target = "usuario", ignore = true)
    })
    PerfilDto toDto(Perfil perfil);

    @Mappings({
            @Mapping(target = "usuario", ignore = true)
    })
    Perfil toEntity(PerfilDto dto);
}
