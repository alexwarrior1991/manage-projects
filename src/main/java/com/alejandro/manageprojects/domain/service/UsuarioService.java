package com.alejandro.manageprojects.domain.service;

import com.alejandro.manageprojects.domain.dto.UsuarioDto;

import java.time.LocalDateTime;
import java.util.List;

public interface UsuarioService {
    UsuarioDto create(UsuarioDto dto);
    UsuarioDto update(Long id, UsuarioDto dto);
    UsuarioDto getById(Long id);
    List<UsuarioDto> getAll();
    void delete(Long id);

    // QueryDSL example: by role name and created date range
    List<UsuarioDto> searchByRolAndCreadoEnBetween(String rolNombre, LocalDateTime desde, LocalDateTime hasta);

    // Compleja con Q-types: usuarios con rol, con al menos N tareas en un proyecto
    List<UsuarioDto> findByRolAndMinTareasInProyecto(String rolNombre, Long proyectoId, long minTareas);
}
