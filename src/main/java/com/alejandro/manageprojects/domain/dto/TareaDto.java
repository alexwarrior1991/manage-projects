package com.alejandro.manageprojects.domain.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.alejandro.manageprojects.view.View;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class TareaDto {
    @JsonView({View.Get.class})
    private Long id;

    @JsonView({View.Post.class, View.Put.class, View.Get.class})
    @NotBlank(groups = {View.Post.class, View.Put.class}, message = "Debe ingresar el título de la tarea.")
    private String titulo;

    @JsonView({View.Post.class, View.Put.class, View.Get.class})
    private String descripcion;

    @JsonView({View.Post.class, View.Put.class, View.Get.class})
    @NotBlank(groups = {View.Post.class, View.Put.class}, message = "Debe ingresar el estado de la tarea.")
    private String estado;

    @JsonView({View.Post.class, View.Put.class, View.Get.class})
    private LocalDateTime fechaLimite;

    @JsonView({View.Post.class, View.Put.class, View.Get.class})
    private UsuarioDto asignadoA;

    @JsonView({View.Post.class, View.Put.class, View.Get.class})
    @NotNull(groups = {View.Post.class, View.Put.class}, message = "Debe ingresar el proyecto de la tarea.")
    private ProyectoDto proyecto;

    @JsonView({View.Post.class, View.Put.class, View.Get.class})
    private Set<EtiquetaDto> etiquetas;
}
