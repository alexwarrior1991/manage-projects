package com.alejandro.manageprojects.domain.mapper;

import com.alejandro.manageprojects.domain.dto.MiembroProyectoDto;
import com.alejandro.manageprojects.domain.entity.MiembroProyecto;
import com.alejandro.manageprojects.domain.entity.Proyecto;
import com.alejandro.manageprojects.domain.entity.Usuario;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface MiembroProyectoMapper {

    @Mappings({
        @Mapping(target = "usuarioId", source = "usuario.id"),
        @Mapping(target = "proyectoId", source = "proyecto.id")
    })
    MiembroProyectoDto toDto(MiembroProyecto entity);

    @Mappings({
        @Mapping(target = "usuario", expression = "java(fromUsuarioId(dto.getUsuarioId()))"),
        @Mapping(target = "proyecto", expression = "java(fromProyectoId(dto.getProyectoId()))")
    })
    MiembroProyecto toEntity(MiembroProyectoDto dto);

    default Usuario fromUsuarioId(Long id) {
        if (id == null) return null;
        Usuario u = new Usuario();
        u.setId(id);
        return u;
    }

    default Proyecto fromProyectoId(Long id) {
        if (id == null) return null;
        Proyecto p = new Proyecto();
        p.setId(id);
        return p;
    }
}
