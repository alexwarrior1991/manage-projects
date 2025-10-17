package com.alejandro.manageprojects.domain.filter;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class TareaFilter {
    private String titulo = "";
    private String descripcion = "";
    private String estado = ""; // enum name
    private String etiquetaNombre = "";
    private String asignadoEmail = "";
    private String proyectoNombre = "";
    private String searchText = "";

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fechaLimiteDesde;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fechaLimiteHasta;
}
