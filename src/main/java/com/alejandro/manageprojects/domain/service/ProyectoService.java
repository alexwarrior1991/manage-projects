package com.alejandro.manageprojects.domain.service;

import com.alejandro.manageprojects.domain.dto.ProyectoDto;
import com.alejandro.manageprojects.domain.filter.ProyectoFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface ProyectoService {
    ProyectoDto create(ProyectoDto dto);
    ProyectoDto update(Long id, ProyectoDto dto);
    ProyectoDto getById(Long id);
    List<ProyectoDto> getAll();
    void delete(Long id);

    List<ProyectoDto> findActivosEnRango(LocalDateTime desde, LocalDateTime hasta);

    Page<ProyectoDto> getProyectos(Pageable page, ProyectoFilter filter);

    // Proyectos filtrados por condiciones de Tarea (Q-classes)
    List<ProyectoDto> findByTareasConEstadoYRangoQ(
            String estado,
            LocalDateTime fechaInicioDesde,
            LocalDateTime fechaFinHasta,
            String tituloContiene,
            String asignadoEmail,
            String etiquetaNombre);

    // Variante con PathBuilder/stream path
    List<ProyectoDto> findByTareasConEstadoYRangoStream(
            String estado,
            LocalDateTime fechaInicioDesde,
            LocalDateTime fechaFinHasta,
            String tituloContiene,
            String asignadoEmail,
            String etiquetaNombre);

    // Q-class bulk update: cerrar proyectos con todas las tareas completadas
    long cerrarProyectosConTareasCompletadas(LocalDateTime fechaFin);

    // Q-class bulk delete: eliminar proyectos sin tareas dentro de un rango de fechas
    long eliminarProyectosSinTareasEntreFechas(LocalDateTime desde, LocalDateTime hasta);

    // Q-class paginada: proyectos por rol de miembro y email del usuario
    Page<ProyectoDto> getProyectosPorMiembros(Pageable page, String rolEnProyecto, String emailLike);

    // Q-class compleja: proyectos con al menos N tareas en un estado y con etiqueta opcional
    List<ProyectoDto> findConMinTareasPorEstado(String estado, long minTareas, String etiquetaNombre);

    // Q-class bulk update: reabrir proyectos (fechaFin = null) si existen tareas no completadas
    long reabrirProyectosConTareasPendientes();

    // Q-class bulk update: inicializar fechaInicio cuando es null y tiene tareas
    long inicializarFechaInicioSiTieneTareas(LocalDateTime fechaInicio);

    // Q-class consulta: proyectos sin comentarios pero con un miembro por rol
    List<ProyectoDto> findSinComentariosConMiembroRol(String rolEnProyecto);

    // Q-class bulk delete: eliminar proyectos con presupuesto gastado > montoTotal
    long eliminarProyectosConPresupuestoInconsistente();
}
