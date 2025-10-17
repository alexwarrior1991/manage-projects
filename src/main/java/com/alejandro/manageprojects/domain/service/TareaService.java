package com.alejandro.manageprojects.domain.service;

import com.alejandro.manageprojects.domain.dto.TareaDto;
import com.alejandro.manageprojects.domain.filter.TareaFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface TareaService {
    TareaDto create(TareaDto dto);
    TareaDto update(Long id, TareaDto dto);
    TareaDto getById(Long id);
    List<TareaDto> getAll();
    void delete(Long id);

    List<TareaDto> search(String estado, Long etiquetaId, Long asignadoId, Long proyectoId);

    Page<TareaDto> getTareas(Pageable page, TareaFilter filter);

    // Q-class bulk update: actualizar estado por proyecto y fecha límite antes de una fecha
    long actualizarEstadoPorProyectoYFecha(String estadoOrigen, String estadoDestino, Long proyectoId, LocalDateTime fechaLimiteAntes);

    // Q-class bulk delete: eliminar por estado y fecha límite antes de una fecha
    long eliminarPorEstadoYFechaLimiteAntes(String estado, LocalDateTime fechaLimiteAntes);

    // Q-class bulk update: reasignar tareas de un usuario a otro (opcionalmente filtrando por proyecto y estado)
    long reasignarTareasDeUsuario(Long fromUsuarioId, Long toUsuarioId, Long proyectoId, String estado);
}
