package com.alejandro.manageprojects.web.controller;

import com.alejandro.manageprojects.domain.dto.TareaDto;
import com.alejandro.manageprojects.domain.filter.TareaFilter;
import com.alejandro.manageprojects.domain.service.TareaService;
import com.alejandro.manageprojects.view.View;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/tareas")
public class TareaController {

    private final TareaService tareaService;

    public TareaController(TareaService tareaService) {
        this.tareaService = tareaService;
    }

    @JsonView(View.Post.class)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TareaDto> create(
            @JsonView(View.Post.class)
            @Validated(View.Post.class) @RequestBody TareaDto dto) {
        return ResponseEntity.ok(tareaService.create(dto));
    }

    @JsonView(View.Put.class)
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TareaDto> update(
            @PathVariable Long id,
            @JsonView(View.Put.class)
            @Validated(View.Put.class) @RequestBody TareaDto dto) {
        return ResponseEntity.ok(tareaService.update(id, dto));
    }

    @JsonView(View.Get.class)
    @GetMapping(path = "/{id}")
    public ResponseEntity<TareaDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(tareaService.getById(id));
    }

    @JsonView(View.Get.class)
    @GetMapping
    public ResponseEntity<List<TareaDto>> getAll() {
        return ResponseEntity.ok(tareaService.getAll());
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tareaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @JsonView(View.Get.class)
    @GetMapping(path = "/search")
    public ResponseEntity<List<TareaDto>> search(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long etiquetaId,
            @RequestParam(required = false) Long asignadoId,
            @RequestParam(required = false) Long proyectoId) {
        return ResponseEntity.ok(tareaService.search(estado, etiquetaId, asignadoId, proyectoId));
    }

    @JsonView(View.Get.class)
    @GetMapping(path = "/page")
    public ResponseEntity<Page<TareaDto>> getTareas(
            Pageable pageable,
            @ModelAttribute TareaFilter filter) {
        return ResponseEntity.ok(tareaService.getTareas(pageable, filter));
    }

    // Bulk operations with Q-classes
    @PostMapping(path = "/bulk/update-estado")
    public ResponseEntity<Long> actualizarEstadoPorProyectoYFecha(
            @RequestParam(required = false) String estadoOrigen,
            @RequestParam String estadoDestino,
            @RequestParam(required = false) Long proyectoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaLimiteAntes) {
        return ResponseEntity.ok(tareaService.actualizarEstadoPorProyectoYFecha(estadoOrigen, estadoDestino, proyectoId, fechaLimiteAntes));
    }

    @DeleteMapping(path = "/bulk/by-estado-fecha")
    public ResponseEntity<Long> eliminarPorEstadoYFechaLimiteAntes(
            @RequestParam String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaLimiteAntes) {
        return ResponseEntity.ok(tareaService.eliminarPorEstadoYFechaLimiteAntes(estado, fechaLimiteAntes));
    }

    @PostMapping(path = "/bulk/reasignar")
    public ResponseEntity<Long> reasignarTareas(
            @RequestParam Long fromUsuarioId,
            @RequestParam Long toUsuarioId,
            @RequestParam(required = false) Long proyectoId,
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(tareaService.reasignarTareasDeUsuario(fromUsuarioId, toUsuarioId, proyectoId, estado));
    }
}
