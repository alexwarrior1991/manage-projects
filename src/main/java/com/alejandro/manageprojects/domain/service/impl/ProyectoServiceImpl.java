package com.alejandro.manageprojects.domain.service.impl;

import com.alejandro.manageprojects.domain.dto.ProyectoDto;
import com.alejandro.manageprojects.domain.entity.*;
import com.alejandro.manageprojects.domain.filter.ProyectoFilter;
import com.alejandro.manageprojects.domain.mapper.ProyectoMapper;
import com.alejandro.manageprojects.domain.repository.ProyectoRepository;
import com.alejandro.manageprojects.domain.service.ProyectoService;
import com.alejandro.manageprojects.web.error.NotFoundException;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.JPAExpressions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProyectoServiceImpl implements ProyectoService {

    private final ProyectoRepository proyectoRepository;
    private final ProyectoMapper proyectoMapper;
    private final JPAQueryFactory queryFactory;

    @Override
    @org.springframework.cache.annotation.CachePut(value = "proyecto", key = "#result.id")
    @org.springframework.cache.annotation.CacheEvict(value = "proyectos_all", key = "'all'")
    public ProyectoDto create(ProyectoDto dto) {
            Proyecto entity = proyectoMapper.toEntity(dto);
            Proyecto saved = proyectoRepository.save(entity);
            return proyectoMapper.toDto(saved);
        }

    @Override
    @org.springframework.cache.annotation.CachePut(value = "proyecto", key = "#id")
    @org.springframework.cache.annotation.CacheEvict(value = "proyectos_all", key = "'all'")
    public ProyectoDto update(Long id, ProyectoDto dto) {
            Proyecto entity = proyectoRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Proyecto no encontrado: " + id));
            entity.setNombre(dto.getNombre());
            entity.setDescripcion(dto.getDescripcion());
            entity.setFechaInicio(dto.getFechaInicio());
            entity.setFechaFin(dto.getFechaFin());
            Proyecto saved = proyectoRepository.save(entity);
            return proyectoMapper.toDto(saved);
        }

    @Override
    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "proyecto", key = "#id")
    public ProyectoDto getById(Long id) {
            return proyectoRepository.findById(id)
                    .map(proyectoMapper::toDto)
                    .orElseThrow(() -> new NotFoundException("Proyecto no encontrado: " + id));
        }

    @Override
    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "proyectos_all", key = "'all'")
    public List<ProyectoDto> getAll() {
            return proyectoRepository.findAll().stream().map(proyectoMapper::toDto).toList();
        }

    @Override
    @org.springframework.cache.annotation.CacheEvict(value = {"proyecto", "proyectos_all"}, allEntries = true, key = "#id")
    public void delete(Long id) {
            if (!proyectoRepository.existsById(id)) {
                throw new NotFoundException("Proyecto no encontrado: " + id);
            }
            proyectoRepository.deleteById(id);
        }

    @Override
    @Transactional(readOnly = true)
    public List<ProyectoDto> findActivosEnRango(LocalDateTime desde, LocalDateTime hasta) {
            log.info("[ProyectoService] findActivosEnRango - Filtra proyectos activos superponiendo rango de fechas (inicio <= hasta y fin >= desde) usando PathBuilder.");
        PathBuilder<Proyecto> p = new PathBuilder<>(Proyecto.class, "proyecto");
        BooleanExpression inicioAntes = (hasta == null) ? null : p.getDateTime("fechaInicio", LocalDateTime.class).loe(hasta);
        BooleanExpression finDespues = (desde == null) ? null : p.getDateTime("fechaFin", LocalDateTime.class).goe(desde);
        BooleanExpression predicate = combineAll(inicioAntes, finDespues);
        Iterable<Proyecto> it = (predicate == null) ? proyectoRepository.findAll() : proyectoRepository.findAll(predicate);
        List<ProyectoDto> list = StreamSupport.stream(it.spliterator(), false)
                .map(proyectoMapper::toDto)
                .collect(Collectors.toList());
        log.debug("[ProyectoService] findActivosEnRango - Predicado: {}", predicate);
        log.info("[ProyectoService] findActivosEnRango - Proyectos encontrados: {}", list.size());
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProyectoDto> getProyectos(Pageable page, ProyectoFilter filter) {
            log.info("[ProyectoService] getProyectos - Construye consulta dinámica con QProyecto aplicando filtros por nombre, descripción, rangos de fechas, presupuesto, email de miembro y búsqueda libre. Soporta paginación y orden.");
        BooleanBuilder qb = new BooleanBuilder();
        QProyecto p = QProyecto.proyecto;

        if (filter != null) {
            if (filter.getNombre() != null && !filter.getNombre().isEmpty()) {
                qb.and(p.nombre.containsIgnoreCase(filter.getNombre()));
            }
            if (filter.getDescripcion() != null && !filter.getDescripcion().isEmpty()) {
                qb.and(p.descripcion.containsIgnoreCase(filter.getDescripcion()));
            }
            if (filter.getFechaInicioDesde() != null) {
                qb.and(p.fechaInicio.goe(filter.getFechaInicioDesde()));
            }
            if (filter.getFechaInicioHasta() != null) {
                qb.and(p.fechaInicio.loe(filter.getFechaInicioHasta()));
            }
            if (filter.getFechaFinDesde() != null) {
                qb.and(p.fechaFin.goe(filter.getFechaFinDesde()));
            }
            if (filter.getFechaFinHasta() != null) {
                qb.and(p.fechaFin.loe(filter.getFechaFinHasta()));
            }
            if (filter.getPresupuestoMin() != null) {
                qb.and(p.presupuesto().montoTotal.goe(filter.getPresupuestoMin()));
            }
            if (filter.getPresupuestoMax() != null) {
                qb.and(p.presupuesto().montoTotal.loe(filter.getPresupuestoMax()));
            }
            if (filter.getGastadoMax() != null) {
                qb.and(p.presupuesto().gastado.loe(filter.getGastadoMax()));
            }
            if (filter.getMiembroEmail() != null && !filter.getMiembroEmail().isEmpty()) {
                qb.and(p.miembros.any().usuario().email.containsIgnoreCase(filter.getMiembroEmail()));
            }
            if (filter.getSearchText() != null && !filter.getSearchText().isEmpty()) {
                BooleanBuilder or = new BooleanBuilder();
                or.or(p.nombre.containsIgnoreCase(filter.getSearchText()))
                  .or(p.descripcion.containsIgnoreCase(filter.getSearchText()))
                  .or(p.tareas.any().titulo.containsIgnoreCase(filter.getSearchText()))
                  .or(p.hitos.any().nombre.containsIgnoreCase(filter.getSearchText()))
                  .or(p.comentarios.any().contenido.containsIgnoreCase(filter.getSearchText()));
                qb.and(or);
            }
        }

        log.debug("[ProyectoService] getProyectos - Predicado construido: {}", qb.getValue());
        Page<Proyecto> res = proyectoRepository.findAll(qb, page);
        log.info("[ProyectoService] getProyectos - Resultado paginado: totalElementos={}, pageSize={}, pageNumber={}", res.getTotalElements(), page.getPageSize(), page.getPageNumber());
        return res.map(proyectoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProyectoDto> findByTareasConEstadoYRangoQ(String estado,
                                                          LocalDateTime fechaInicioDesde,
                                                          LocalDateTime fechaFinHasta,
                                                          String tituloContiene,
                                                          String asignadoEmail,
                                                          String etiquetaNombre) {
        log.info("[ProyectoService] findByTareasConEstadoYRangoQ - Usa QProyecto/QTarea con left join sobre tareas y filtros por estado, título, asignado, etiqueta y rango de fechas del proyecto. Devuelve proyectos distintos.");
        QProyecto p = QProyecto.proyecto;
        QTarea t = QTarea.tarea;
        BooleanBuilder builder = new BooleanBuilder();

        if (fechaInicioDesde != null) {
            builder.and(p.fechaInicio.goe(fechaInicioDesde));
        }
        if (fechaFinHasta != null) {
            builder.and(p.fechaFin.loe(fechaFinHasta));
        }
        if (estado != null && !estado.isEmpty()) {
            try {
                builder.and(t.estado.eq(Tarea.Estado.valueOf(estado)));
            } catch (IllegalArgumentException ignored) {}
        }
        if (tituloContiene != null && !tituloContiene.isEmpty()) {
            builder.and(t.titulo.containsIgnoreCase(tituloContiene));
        }
        if (asignadoEmail != null && !asignadoEmail.isEmpty()) {
            builder.and(t.asignadoA().email.containsIgnoreCase(asignadoEmail));
        }
        if (etiquetaNombre != null && !etiquetaNombre.isEmpty()) {
            builder.and(t.etiquetas.any().nombre.containsIgnoreCase(etiquetaNombre));
        }

        log.debug("[ProyectoService] findByTareasConEstadoYRangoQ - Predicado sobre tareas/proyecto: {}", builder.getValue());
        List<Proyecto> proyectos = queryFactory
                .selectDistinct(p)
                .from(p)
                .leftJoin(p.tareas, t)
                .where(builder)
                .fetch();
        log.info("[ProyectoService] findByTareasConEstadoYRangoQ - Proyectos encontrados: {}", proyectos.size());

        return proyectos.stream().map(proyectoMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProyectoDto> findByTareasConEstadoYRangoStream(String estado,
                                                               LocalDateTime fechaInicioDesde,
                                                               LocalDateTime fechaFinHasta,
                                                               String tituloContiene,
                                                               String asignadoEmail,
                                                               String etiquetaNombre) {
        log.info("[ProyectoService] findByTareasConEstadoYRangoStream - Usa PathBuilder (stream path) navegando a tareas.any() con filtros por estado, título, asignado y etiqueta, combinado con rango de fechas de proyecto.");
        PathBuilder<Proyecto> p = new PathBuilder<>(Proyecto.class, "proyecto");
        BooleanExpression predicate = null;

        // Filtros de fechas del proyecto
        BooleanExpression byInicio = (fechaInicioDesde == null) ? null : p.getDateTime("fechaInicio", LocalDateTime.class).goe(fechaInicioDesde);
        BooleanExpression byFin = (fechaFinHasta == null) ? null : p.getDateTime("fechaFin", LocalDateTime.class).loe(fechaFinHasta);
        predicate = combineAll(byInicio, byFin);

        // Subruta a tareas (any)
        var tareasAny = p.getSet("tareas", Tarea.class).any();

        BooleanExpression byEstado = null;
        if (estado != null && !estado.isEmpty()) {
            try {
                byEstado = tareasAny.getEnum("estado", Tarea.Estado.class)
                        .eq(Tarea.Estado.valueOf(estado));
            } catch (IllegalArgumentException ignored) {}
        }
        BooleanExpression byTitulo = (tituloContiene == null || tituloContiene.isEmpty()) ? null :
                tareasAny.getString("titulo").containsIgnoreCase(tituloContiene);

        BooleanExpression byAsignado = null;
        if (asignadoEmail != null && !asignadoEmail.isEmpty()) {
            byAsignado = tareasAny.get("asignadoA", Usuario.class)
                    .getString("email").containsIgnoreCase(asignadoEmail);
        }
        BooleanExpression byEtiqueta = null;
        if (etiquetaNombre != null && !etiquetaNombre.isEmpty()) {
            byEtiqueta = tareasAny.getSet("etiquetas", Etiqueta.class)
                    .any().getString("nombre").containsIgnoreCase(etiquetaNombre);
        }

        predicate = combineAll(predicate, byEstado, byTitulo, byAsignado, byEtiqueta);

        Iterable<Proyecto> it = (predicate == null) ? proyectoRepository.findAll() : proyectoRepository.findAll(predicate);
        List<ProyectoDto> dtos = StreamSupport.stream(it.spliterator(), false)
                .map(proyectoMapper::toDto)
                .collect(Collectors.toList());
        log.debug("[ProyectoService] findByTareasConEstadoYRangoStream - Predicado: {}", predicate);
        log.info("[ProyectoService] findByTareasConEstadoYRangoStream - Proyectos encontrados: {}", dtos.size());
        return dtos;
    }

    @Override
    public long cerrarProyectosConTareasCompletadas(LocalDateTime fechaFin) {
        QProyecto p = QProyecto.proyecto;
        QTarea t = QTarea.tarea;

        // Actualiza fechaFin donde NO existan tareas no completadas
        log.info("[ProyectoService] cerrarProyectosConTareasCompletadas - Actualiza fechaFin={} en proyectos donde no existen tareas pendientes (NOT EXISTS tareas con estado != COMPLETADA).", fechaFin);
        long updated = queryFactory.update(p)
                .set(p.fechaFin, fechaFin)
                .where(
                        JPAExpressions.selectOne()
                                .from(t)
                                .where(t.proyecto().id.eq(p.id)
                                        .and(t.estado.ne(Tarea.Estado.COMPLETADA)))
                                .notExists()
                )
                .execute();
        log.info("[ProyectoService] cerrarProyectosConTareasCompletadas - Proyectos actualizados: {}", updated);
        return updated;
    }

    @Override
    public long eliminarProyectosSinTareasEntreFechas(LocalDateTime desde, LocalDateTime hasta) {
        QProyecto p = QProyecto.proyecto;
        QTarea t = QTarea.tarea;
        BooleanBuilder builder = new BooleanBuilder();
        if (desde != null) {
            builder.and(p.fechaInicio.goe(desde));
        }
        if (hasta != null) {
            builder.and(p.fechaFin.loe(hasta));
        }
        // Borrar proyectos que no tengan ninguna tarea
        builder.and(
                JPAExpressions.selectOne().from(t)
                        .where(t.proyecto().id.eq(p.id))
                        .notExists()
        );
        log.debug("[ProyectoService] eliminarProyectosSinTareasEntreFechas - Predicado: {}", builder.getValue());
        long deleted = queryFactory.delete(p)
                .where(builder)
                .execute();
        log.info("[ProyectoService] eliminarProyectosSinTareasEntreFechas - Proyectos eliminados: {}", deleted);
        return deleted;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProyectoDto> getProyectosPorMiembros(Pageable page, String rolEnProyecto, String emailLike) {
            log.info("[ProyectoService] getProyectosPorMiembros - Filtra con QProyecto por rol de miembro y email del usuario (containsIgnoreCase). Paginado.");
        QProyecto p = QProyecto.proyecto;
        BooleanBuilder qb = new BooleanBuilder();
        if (rolEnProyecto != null && !rolEnProyecto.isEmpty()) {
            qb.and(p.miembros.any().rolEnProyecto.equalsIgnoreCase(rolEnProyecto));
        }
        if (emailLike != null && !emailLike.isEmpty()) {
            qb.and(p.miembros.any().usuario().email.containsIgnoreCase(emailLike));
        }
        Page<Proyecto> res = proyectoRepository.findAll(qb, page);
        log.debug("[ProyectoService] getProyectosPorMiembros - Predicado: {}", qb.getValue());
        log.info("[ProyectoService] getProyectosPorMiembros - Resultado paginado: totalElementos={}, pageSize={}, pageNumber={}", res.getTotalElements(), page.getPageSize(), page.getPageNumber());
        return res.map(proyectoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProyectoDto> findConMinTareasPorEstado(String estado, long minTareas, String etiquetaNombre) {
        QProyecto p = QProyecto.proyecto;
        QTarea t = QTarea.tarea;
        BooleanBuilder where = new BooleanBuilder();
        if (estado != null && !estado.isEmpty()) {
            try {
                where.and(t.estado.eq(Tarea.Estado.valueOf(estado)));
            } catch (IllegalArgumentException ignored) {}
        }
        if (etiquetaNombre != null && !etiquetaNombre.isEmpty()) {
            where.and(t.etiquetas.any().nombre.containsIgnoreCase(etiquetaNombre));
        }
        List<Proyecto> proyectos = queryFactory
                .select(p)
                .from(p)
                .leftJoin(p.tareas, t)
                .where(where)
                .groupBy(p.id)
                .having(t.count().goe(minTareas))
                .fetch();
        return proyectos.stream().map(proyectoMapper::toDto).toList();
    }

    @Override
    public long reabrirProyectosConTareasPendientes() {
        QProyecto p = QProyecto.proyecto;
        QTarea t = QTarea.tarea;
        return queryFactory.update(p)
                .set(p.fechaFin, (LocalDateTime) null)
                .where(
                        JPAExpressions.selectOne()
                                .from(t)
                                .where(t.proyecto().id.eq(p.id)
                                        .and(t.estado.ne(Tarea.Estado.COMPLETADA)))
                                .exists()
                )
                .execute();
    }

    @Override
    public long inicializarFechaInicioSiTieneTareas(LocalDateTime fechaInicio) {
        QProyecto p = QProyecto.proyecto;
        QTarea t = QTarea.tarea;
        return queryFactory.update(p)
                .set(p.fechaInicio, fechaInicio)
                .where(
                        p.fechaInicio.isNull()
                                .and(
                                        JPAExpressions.selectOne().from(t)
                                                .where(t.proyecto().id.eq(p.id))
                                                .exists()
                                )
                )
                .execute();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProyectoDto> findSinComentariosConMiembroRol(String rolEnProyecto) {
        QProyecto p = QProyecto.proyecto;
        QComentario c = QComentario.comentario;
        BooleanBuilder where = new BooleanBuilder();
        if (rolEnProyecto != null && !rolEnProyecto.isEmpty()) {
            where.and(p.miembros.any().rolEnProyecto.equalsIgnoreCase(rolEnProyecto));
        }
        // NOT EXISTS comentarios
        where.and(
                JPAExpressions.selectOne().from(c)
                        .where(c.proyecto().id.eq(p.id))
                        .notExists()
        );
        List<Proyecto> proyectos = queryFactory.selectFrom(p)
                .where(where)
                .fetch();
        return proyectos.stream().map(proyectoMapper::toDto).toList();
    }

    @Override
    public long eliminarProyectosConPresupuestoInconsistente() {
        QProyecto p = QProyecto.proyecto;
        return queryFactory.delete(p)
                .where(p.presupuesto().gastado.isNotNull()
                        .and(p.presupuesto().montoTotal.isNotNull())
                        .and(p.presupuesto().gastado.gt(p.presupuesto().montoTotal)))
                .execute();
    }

    private BooleanExpression combineAll(BooleanExpression... parts) {
        BooleanExpression result = null;
        for (BooleanExpression p : parts) {
            if (p == null) continue;
            result = (result == null) ? p : result.and(p);
        }
        return result;
    }
}
