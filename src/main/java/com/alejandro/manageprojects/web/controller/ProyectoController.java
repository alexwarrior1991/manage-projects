package com.alejandro.manageprojects.web.controller;

import com.alejandro.manageprojects.domain.dto.ProyectoDto;
import com.alejandro.manageprojects.domain.filter.ProyectoFilter;
import com.alejandro.manageprojects.domain.service.ProyectoService;
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
@RequestMapping(path = "/api/v1/proyectos")
public class ProyectoController {

    private final ProyectoService proyectoService;

    public ProyectoController(ProyectoService proyectoService) {
        this.proyectoService = proyectoService;
    }

    @JsonView(View.Post.class)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProyectoDto> create(
            @JsonView(View.Post.class)
            @Validated(View.Post.class) @RequestBody ProyectoDto dto) {
        return ResponseEntity.ok(proyectoService.create(dto));
    }

    @JsonView(View.Put.class)
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProyectoDto> update(
            @PathVariable Long id,
            @JsonView(View.Put.class)
            @Validated(View.Put.class) @RequestBody ProyectoDto dto) {
        return ResponseEntity.ok(proyectoService.update(id, dto));
    }

    @JsonView(View.Get.class)
    @GetMapping(path = "/{id}")
    public ResponseEntity<ProyectoDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(proyectoService.getById(id));
    }

    @JsonView(View.Get.class)
    @GetMapping
    public ResponseEntity<List<ProyectoDto>> getAll() {
        return ResponseEntity.ok(proyectoService.getAll());
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        proyectoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @JsonView(View.Get.class)
    @GetMapping(path = "/activos")
    public ResponseEntity<List<ProyectoDto>> activosEnRango(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return ResponseEntity.ok(proyectoService.findActivosEnRango(desde, hasta));
    }

    @JsonView(View.Get.class)
    @GetMapping(path = "/page")
    public ResponseEntity<Page<ProyectoDto>> getProyectos(Pageable pageable,
                                                          @ModelAttribute ProyectoFilter filter) {
        return ResponseEntity.ok(proyectoService.getProyectos(pageable, filter));
    }

    @JsonView(View.Get.class)
    @GetMapping(path = "/por-tareas")
    public ResponseEntity<List<ProyectoDto>> getProyectosPorTareasQ(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicioDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFinHasta,
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String asignadoEmail,
            @RequestParam(required = false) String etiquetaNombre) {
        return ResponseEntity.ok(
                proyectoService.findByTareasConEstadoYRangoQ(estado, fechaInicioDesde, fechaFinHasta, titulo, asignadoEmail, etiquetaNombre)
        );
    }

    @JsonView(View.Get.class)
    @GetMapping(path = "/por-tareas-stream")
    public ResponseEntity<List<ProyectoDto>> getProyectosPorTareasStream(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicioDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFinHasta,
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String asignadoEmail,
            @RequestParam(required = false) String etiquetaNombre) {
        return ResponseEntity.ok(
                proyectoService.findByTareasConEstadoYRangoStream(estado, fechaInicioDesde, fechaFinHasta, titulo, asignadoEmail, etiquetaNombre)
        );
    }

    @PostMapping(path = "/cerrar-completos")
    public ResponseEntity<Long> cerrarProyectosCompletos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        return ResponseEntity.ok(proyectoService.cerrarProyectosConTareasCompletadas(fechaFin));
    }

    @DeleteMapping(path = "/sin-tareas")
    public ResponseEntity<Long> eliminarProyectosSinTareas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return ResponseEntity.ok(proyectoService.eliminarProyectosSinTareasEntreFechas(desde, hasta));
    }

    @JsonView(View.Get.class)
    @GetMapping(path = "/by-miembros")
    public ResponseEntity<Page<ProyectoDto>> getProyectosPorMiembros(
            Pageable pageable,
            @RequestParam(required = false) String rolEnProyecto,
            @RequestParam(required = false) String emailLike) {
        return ResponseEntity.ok(proyectoService.getProyectosPorMiembros(pageable, rolEnProyecto, emailLike));
    }

    @JsonView(View.Get.class)
    @GetMapping(path = "/min-tareas")
    public ResponseEntity<List<ProyectoDto>> getProyectosConMinTareas(
            @RequestParam String estado,
            @RequestParam long minTareas,
            @RequestParam(required = false) String etiquetaNombre) {
        return ResponseEntity.ok(proyectoService.findConMinTareasPorEstado(estado, minTareas, etiquetaNombre));
    }

    @PostMapping(path = "/reabrir-pendientes")
    public ResponseEntity<Long> reabrirProyectosConPendientes() {
        return ResponseEntity.ok(proyectoService.reabrirProyectosConTareasPendientes());
    }

    @PostMapping(path = "/init-fecha-inicio")
    public ResponseEntity<Long> initFechaInicio(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fecha) {
        return ResponseEntity.ok(proyectoService.inicializarFechaInicioSiTieneTareas(fecha));
    }

    @JsonView(View.Get.class)
    @GetMapping(path = "/sin-comentarios-miembro")
    public ResponseEntity<List<ProyectoDto>> getProyectosSinComentariosConMiembroRol(
            @RequestParam String rol) {
        return ResponseEntity.ok(proyectoService.findSinComentariosConMiembroRol(rol));
    }

    @DeleteMapping(path = "/presupuesto-inconsistente")
    public ResponseEntity<Long> eliminarPresupuestosInconsistentes() {
        return ResponseEntity.ok(proyectoService.eliminarProyectosConPresupuestoInconsistente());
    }
}
