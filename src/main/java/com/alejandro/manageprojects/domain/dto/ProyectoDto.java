package com.alejandro.manageprojects.domain.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.alejandro.manageprojects.view.View;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import jakarta.validation.constraints.NotBlank;

@Data
public class ProyectoDto {
    @JsonView({View.Get.class})
    private Long id;

    @JsonView({View.Post.class, View.Put.class, View.Get.class})
    @NotBlank(groups = {View.Post.class, View.Put.class}, message = "Debe ingresar el nombre del proyecto.")
    private String nombre;

    @JsonView({View.Post.class, View.Put.class, View.Get.class})
    private String descripcion;

    @JsonView({View.Post.class, View.Put.class, View.Get.class})
    private LocalDateTime fechaInicio;

    @JsonView({View.Post.class, View.Put.class, View.Get.class})
    private LocalDateTime fechaFin;

    @JsonView({View.Post.class, View.Put.class, View.Get.class})
    private PresupuestoDto presupuesto;

    @JsonView({View.Get.class})
    private Set<TareaDto> tareas;

    @JsonView({View.Get.class})
    private Set<ComentarioDto> comentarios;

    @JsonView({View.Get.class})
    private Set<HitoDto> hitos;
}
