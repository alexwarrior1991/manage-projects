package com.alejandro.manageprojects.domain.service.impl;

import com.alejandro.manageprojects.domain.dto.TareaDto;
import com.alejandro.manageprojects.domain.entity.Etiqueta;
import com.alejandro.manageprojects.domain.entity.QTarea;
import com.alejandro.manageprojects.domain.entity.QUsuario;
import com.alejandro.manageprojects.domain.entity.Tarea;
import com.alejandro.manageprojects.domain.entity.Usuario;
import com.alejandro.manageprojects.domain.filter.TareaFilter;
import com.alejandro.manageprojects.domain.mapper.TareaMapper;
import com.alejandro.manageprojects.domain.repository.TareaRepository;
import com.alejandro.manageprojects.domain.service.TareaService;
import com.alejandro.manageprojects.web.error.NotFoundException;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class TareaServiceImpl implements TareaService {

    private final TareaRepository tareaRepository;
    private final TareaMapper tareaMapper;
    private final JPAQueryFactory queryFactory;

    @Override
    @CachePut(value = "tarea", key = "#result.id")
    @CacheEvict(value = "tareas_all", key = "'all'")
    public TareaDto create(TareaDto dto) {
            Tarea entity = tareaMapper.toEntity(dto);
            Tarea saved = tareaRepository.save(entity);
            return tareaMapper.toDto(saved);
        }

    @Override
    @CachePut(value = "tarea", key = "#id")
    @CacheEvict(value = "tareas_all", key = "'all'")
    public TareaDto update(Long id, TareaDto dto) {
            Tarea entity = tareaRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Tarea no encontrada: " + id));
            entity.setTitulo(dto.getTitulo());
            entity.setDescripcion(dto.getDescripcion());
            entity.setEstado(tareaMapper.toEstado(dto.getEstado()));
            entity.setFechaLimite(dto.getFechaLimite());
            Tarea saved = tareaRepository.save(entity);
            return tareaMapper.toDto(saved);
        }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tarea", key = "#id")
    public TareaDto getById(Long id) {
            return tareaRepository.findById(id)
                    .map(tareaMapper::toDto)
                    .orElseThrow(() -> new NotFoundException("Tarea no encontrada: " + id));
        }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tareas_all", key = "'all'")
    public List<TareaDto> getAll() {
            return tareaRepository.findAll().stream().map(tareaMapper::toDto).toList();
        }

    @Override
    @org.springframework.cache.annotation.CacheEvict(value = {"tarea", "tareas_all"}, allEntries = true, key = "#id")
    public void delete(Long id) {
            if (!tareaRepository.existsById(id)) {
                throw new NotFoundException("Tarea no encontrada: " + id);
            }
            tareaRepository.deleteById(id);
        }

    @Override
    @Transactional(readOnly = true)
    public List<TareaDto> search(String estado, Long etiquetaId, Long asignadoId, Long proyectoId) {
            log.info("[TareaService] search - Usa PathBuilder para construir filtros por estado, etiqueta (ManyToMany), asignado (ManyToOne) y proyecto (ManyToOne).");
        PathBuilder<Tarea> t = new PathBuilder<>(Tarea.class, "tarea");
        BooleanExpression byEstado = null;
        if (estado != null) {
            Tarea.Estado est = Tarea.Estado.valueOf(estado);
            byEstado = t.getEnum("estado", Tarea.Estado.class).eq(est);
        }
        BooleanExpression byEtiqueta = (etiquetaId == null) ? null : t.getSet("etiquetas", Etiqueta.class)
                .any().getNumber("id", Long.class).eq(etiquetaId);
        BooleanExpression byAsignado = (asignadoId == null) ? null : t.getNumber("asignadoA.id", Long.class).eq(asignadoId);
        BooleanExpression byProyecto = (proyectoId == null) ? null : t.getNumber("proyecto.id", Long.class).eq(proyectoId);
        BooleanExpression predicate = combineAll(byEstado, byEtiqueta, byAsignado, byProyecto);
        Iterable<Tarea> it = (predicate == null) ? tareaRepository.findAll() : tareaRepository.findAll(predicate);
        List<TareaDto> list = StreamSupport.stream(it.spliterator(), false)
                .map(tareaMapper::toDto)
                .collect(Collectors.toList());
        log.debug("[TareaService] search - Predicado: {}", predicate);
        log.info("[TareaService] search - Tareas encontradas: {} (estado={}, etiquetaId={}, asignadoId={}, proyectoId={})", list.size(), estado, etiquetaId, asignadoId, proyectoId);
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TareaDto> getTareas(Pageable page, TareaFilter filter) {
            log.info("[TareaService] getTareas - Construye BooleanBuilder con QTarea aplicando filtros por título, descripción, estado, etiqueta, asignado, proyecto, fecha límite y búsqueda libre. Soporta paginación.");
        BooleanBuilder qb = new BooleanBuilder();
        QTarea t = QTarea.tarea;

        if (filter != null) {
            if (filter.getTitulo() != null && !filter.getTitulo().isEmpty()) {
                qb.and(t.titulo.containsIgnoreCase(filter.getTitulo()));
            }
            if (filter.getDescripcion() != null && !filter.getDescripcion().isEmpty()) {
                qb.and(t.descripcion.containsIgnoreCase(filter.getDescripcion()));
            }
            if (filter.getEstado() != null && !filter.getEstado().isEmpty()) {
                try {
                    qb.and(t.estado.eq(Tarea.Estado.valueOf(filter.getEstado())));
                } catch (IllegalArgumentException ignored) {}
            }
            if (filter.getEtiquetaNombre() != null && !filter.getEtiquetaNombre().isEmpty()) {
                qb.and(t.etiquetas.any().nombre.containsIgnoreCase(filter.getEtiquetaNombre()));
            }
            if (filter.getAsignadoEmail() != null && !filter.getAsignadoEmail().isEmpty()) {
                qb.and(t.asignadoA().email.containsIgnoreCase(filter.getAsignadoEmail()));
            }
            if (filter.getProyectoNombre() != null && !filter.getProyectoNombre().isEmpty()) {
                qb.and(t.proyecto().nombre.containsIgnoreCase(filter.getProyectoNombre()));
            }
            if (filter.getFechaLimiteDesde() != null) {
                qb.and(t.fechaLimite.goe(filter.getFechaLimiteDesde()));
            }
            if (filter.getFechaLimiteHasta() != null) {
                qb.and(t.fechaLimite.loe(filter.getFechaLimiteHasta()));
            }
            if (filter.getSearchText() != null && !filter.getSearchText().isEmpty()) {
                BooleanBuilder or = new BooleanBuilder();
                or.or(t.titulo.containsIgnoreCase(filter.getSearchText()))
                  .or(t.descripcion.containsIgnoreCase(filter.getSearchText()))
                  .or(t.proyecto().nombre.containsIgnoreCase(filter.getSearchText()))
                  .or(t.asignadoA().email.containsIgnoreCase(filter.getSearchText()))
                  .or(t.etiquetas.any().nombre.containsIgnoreCase(filter.getSearchText()));
                qb.and(or);
            }
        }

        log.debug("[TareaService] getTareas - Predicado construido: {}", qb.getValue());
        Page<Tarea> res = tareaRepository.findAll(qb, page);
        log.info("[TareaService] getTareas - Resultado paginado: totalElementos={}, pageSize={}, pageNumber={}", res.getTotalElements(), page.getPageSize(), page.getPageNumber());
        return res.map(tareaMapper::toDto);
    }

    @Override
    @CacheEvict(value = {"tarea", "tareas_all"}, allEntries = true)
    public long actualizarEstadoPorProyectoYFecha(String estadoOrigen, String estadoDestino, Long proyectoId, LocalDateTime fechaLimiteAntes) {
        log.info("[TareaService] actualizarEstadoPorProyectoYFecha - Actualiza estado masivamente usando QTarea con filtros opcionales por estadoOrigen, proyectoId y fechaLimite < fecha dada.");
        QTarea t = QTarea.tarea;
        BooleanBuilder where = new BooleanBuilder();
        if (estadoOrigen != null && !estadoOrigen.isEmpty()) {
            try { where.and(t.estado.eq(Tarea.Estado.valueOf(estadoOrigen))); } catch (IllegalArgumentException ignored) {}
        }
        if (proyectoId != null) {
            where.and(t.proyecto().id.eq(proyectoId));
        }
        if (fechaLimiteAntes != null) {
            where.and(t.fechaLimite.before(fechaLimiteAntes));
        }
        Tarea.Estado nuevoEstado = Tarea.Estado.PENDIENTE;
        if (estadoDestino != null && !estadoDestino.isEmpty()) {
            try { nuevoEstado = Tarea.Estado.valueOf(estadoDestino); } catch (IllegalArgumentException ignored) {}
        }
        long updated = queryFactory.update(t)
                .set(t.estado, nuevoEstado)
                .where(where)
                .execute();
        log.debug("[TareaService] actualizarEstadoPorProyectoYFecha - Predicado: {}", where.getValue());
        log.info("[TareaService] actualizarEstadoPorProyectoYFecha - Tareas actualizadas: {}", updated);
        return updated;
    }

    @Override
    @CacheEvict(value = {"tarea", "tareas_all"}, allEntries = true)
    public long eliminarPorEstadoYFechaLimiteAntes(String estado, LocalDateTime fechaLimiteAntes) {
        log.info("[TareaService] eliminarPorEstadoYFechaLimiteAntes - Elimina en bloque usando QTarea por estado y fecha límite anterior a la dada.");
        QTarea t = QTarea.tarea;
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if (estado != null && !estado.isEmpty()) {
            try { booleanBuilder.and(t.estado.eq(Tarea.Estado.valueOf(estado))); } catch (IllegalArgumentException ignored) {}
        }
        if (fechaLimiteAntes != null) {
            booleanBuilder.and(t.fechaLimite.before(fechaLimiteAntes));
        }
        long deleted = queryFactory.delete(t).where(booleanBuilder).execute();
        log.debug("[TareaService] eliminarPorEstadoYFechaLimiteAntes - Predicado: {}", booleanBuilder.getValue());
        log.info("[TareaService] eliminarPorEstadoYFechaLimiteAntes - Tareas eliminadas: {}", deleted);
        return deleted;
    }

    @Override
    @CacheEvict(value = {"tarea", "tareas_all"}, allEntries = true)
    public long reasignarTareasDeUsuario(Long fromUsuarioId, Long toUsuarioId, Long proyectoId, String estado) {
        log.info("[TareaService] reasignarTareasDeUsuario - Reasigna tareas de un usuario a otro usando QTarea/QUsuario. Filtros opcionales por proyecto y estado.");
        if (fromUsuarioId == null || toUsuarioId == null) {
            throw new IllegalArgumentException("fromUsuarioId y toUsuarioId son obligatorios");
        }
        QTarea t = QTarea.tarea;
        QUsuario u = QUsuario.usuario;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(t.asignadoA().id.eq(fromUsuarioId));
        if (proyectoId != null) {
            builder.and(t.proyecto().id.eq(proyectoId));
        }
        if (estado != null && !estado.isEmpty()) {
            try { builder.and(t.estado.eq(Tarea.Estado.valueOf(estado))); } catch (IllegalArgumentException ignored) {}
        }
        Usuario nuevoAsignado = new Usuario();
        nuevoAsignado.setId(toUsuarioId);
        long updated = queryFactory.update(t)
                .set(t.asignadoA(), nuevoAsignado)
                .where(builder)
                .execute();
        log.debug("[TareaService] reasignarTareasDeUsuario - Predicado: {}", builder.getValue());
        log.info("[TareaService] reasignarTareasDeUsuario - Tareas reasignadas: {}", updated);
        return updated;
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
