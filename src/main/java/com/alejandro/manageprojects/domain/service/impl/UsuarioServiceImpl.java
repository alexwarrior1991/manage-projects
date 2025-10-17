package com.alejandro.manageprojects.domain.service.impl;

import com.alejandro.manageprojects.domain.dto.UsuarioDto;
import com.alejandro.manageprojects.domain.entity.*;
import com.alejandro.manageprojects.domain.mapper.UsuarioMapper;
import com.alejandro.manageprojects.domain.repository.UsuarioRepository;
import com.alejandro.manageprojects.domain.service.UsuarioService;
import com.alejandro.manageprojects.web.error.NotFoundException;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final JPAQueryFactory queryFactory;

    @Override
    @CachePut(value = "usuario", key = "#result.id")
    @CacheEvict(value = "usuarios_all", key = "'all'")
    public UsuarioDto create(UsuarioDto dto) {
            Usuario entity = usuarioMapper.toEntity(dto);
            Usuario saved = usuarioRepository.save(entity);
            return usuarioMapper.toDto(saved);
        }

    @Override
    @CachePut(value = "usuario", key = "#id")
    @org.springframework.cache.annotation.CacheEvict(value = "usuarios_all", key = "'all'")
    public UsuarioDto update(Long id, UsuarioDto dto) {
            Usuario existing = usuarioRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + id));
            // Actualizamos campos básicos (sin tocar colecciones propietarias aquí)
            existing.setNombre(dto.getNombre());
            existing.setApellido(dto.getApellido());
            existing.setEmail(dto.getEmail());
            // MapStruct podría encargarse, pero evitamos reemplazar relaciones complejas para minimalismo
            Usuario saved = usuarioRepository.save(existing);
            return usuarioMapper.toDto(saved);
        }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "usuario", key = "#id")
    public UsuarioDto getById(Long id) {
            return usuarioRepository.findById(id)
                    .map(usuarioMapper::toDto)
                    .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + id));
        }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "usuarios_all", key = "'all'")
    public List<UsuarioDto> getAll() {
            return usuarioRepository.findAll().stream().map(usuarioMapper::toDto).toList();
        }

    @Override
    @CacheEvict(value = {"usuario", "usuarios_all"}, allEntries = true, key = "#id")
    public void delete(Long id) {
            if (!usuarioRepository.existsById(id)) {
                throw new NotFoundException("Usuario no encontrado: " + id);
            }
            usuarioRepository.deleteById(id);
        }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDto> searchByRolAndCreadoEnBetween(String rolNombre, LocalDateTime desde, LocalDateTime hasta) {
            log.info("[UsuarioService] searchByRolAndCreadoEnBetween - Usa QUsuario con filtro por nombre de rol y rango de fechas (creadoEn). Retorna lista mapeada a DTO.");
        BooleanBuilder builder = new BooleanBuilder();
        QUsuario u = QUsuario.usuario;
        if (rolNombre != null && !rolNombre.isEmpty()) {
            builder.and(u.roles.any().nombre.equalsIgnoreCase(rolNombre));
        }
        if (desde != null) {
            builder.and(u.creadoEn.goe(desde));
        }
        if (hasta != null) {
            builder.and(u.creadoEn.loe(hasta));
        }
        Iterable<Usuario> all = usuarioRepository.findAll(builder);
        java.util.List<UsuarioDto> result = StreamSupport.stream(all.spliterator(), false)
                .map(usuarioMapper::toDto)
                .collect(Collectors.toList());
        log.debug("[UsuarioService] searchByRolAndCreadoEnBetween - Predicado: {}", builder.getValue());
        log.info("[UsuarioService] searchByRolAndCreadoEnBetween - Usuarios encontrados: {} (rolNombre={}, desde={}, hasta={})", result.size(), rolNombre, desde, hasta);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDto> findByRolAndMinTareasInProyecto(String rolNombre, Long proyectoId, long minTareas) {
        log.info("[UsuarioService] findByRolAndMinTareasInProyecto - Usa QUsuario/QRol/QTarea con join sobre roles y tareas (opcional por proyecto), agrupación por usuario y having count(tareas) >= minTareas.");
        QUsuario u = QUsuario.usuario;
        QRol r = QRol.rol;
        QTarea t = QTarea.tarea;

        var query = queryFactory.select(u)
                .from(u)
                .join(u.roles, r);

        if (proyectoId != null) {
            query = query.leftJoin(u.tareasAsignadas, t)
                    .where(t.proyecto().id.eq(proyectoId));
        }
        if (rolNombre != null && !rolNombre.isEmpty()) {
            query = query.where(r.nombre.equalsIgnoreCase(rolNombre));
        }

        query = query.groupBy(u.id);
        if (minTareas > 0) {
            query = query.having(t.count().goe(minTareas));
        }

        java.util.List<Usuario> usuarios = query.fetch();
        java.util.List<UsuarioDto> dtos = usuarios.stream().map(usuarioMapper::toDto).toList();
        log.info("[UsuarioService] findByRolAndMinTareasInProyecto - Usuarios encontrados: {} (rolNombre={}, proyectoId={}, minTareas={})", dtos.size(), rolNombre, proyectoId, minTareas);
        return dtos;
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
