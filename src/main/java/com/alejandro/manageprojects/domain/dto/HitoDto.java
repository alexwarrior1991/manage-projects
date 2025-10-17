package com.alejandro.manageprojects.domain.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.alejandro.manageprojects.view.View;
import lombok.Data;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class HitoDto {
    @JsonView({View.Get.class})
    private Long id;

    @JsonView({View.Post.class, View.Put.class, View.Get.class})
    @NotBlank(groups = {View.Post.class, View.Put.class}, message = "Debe ingresar el nombre del hito.")
    private String nombre;

    @JsonView({View.Post.class, View.Put.class, View.Get.class})
    private String descripcion;

    @JsonView({View.Post.class, View.Put.class, View.Get.class})
    private LocalDateTime fechaObjetivo;

    @JsonView({View.Post.class, View.Put.class, View.Get.class})
    @NotNull(groups = {View.Post.class, View.Put.class}, message = "Debe ingresar el proyecto del hito.")
    private ProyectoDto proyecto;
}
