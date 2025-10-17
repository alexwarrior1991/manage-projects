package com.alejandro.manageprojects.domain.filter;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProyectoFilter {
    private String nombre = "";
    private String descripcion = "";
    private String miembroEmail = ""; // miembro del proyecto por email de usuario
    private String searchText = "";   // búsqueda libre en varios campos

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fechaInicioDesde;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fechaInicioHasta;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fechaFinDesde;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fechaFinHasta;

    // Presupuesto
    private BigDecimal presupuestoMin;   // montoTotal mínimo
    private BigDecimal presupuestoMax;   // montoTotal máximo
    private BigDecimal gastadoMax;       // topo máximo gastado

    // Cantidades relacionadas
    private Integer minTareas;           // mínimo de tareas asociadas
    private Integer minHitos;            // mínimo de hitos asociados
    private Integer minComentarios;      // mínimo de comentarios asociados
}
