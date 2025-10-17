package com.alejandro.manageprojects.domain.mapper;

import com.alejandro.manageprojects.domain.dto.PerfilDto;
import com.alejandro.manageprojects.domain.entity.Perfil;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UsuarioMapper.class})
public interface PerfilMapper {

    PerfilDto toDto(Perfil perfil);

    Perfil toEntity(PerfilDto dto);
}
