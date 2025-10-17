package com.alejandro.manageprojects.domain.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Range;
import jakarta.validation.constraints.NotNull;
import com.alejandro.manageprojects.view.View;

@Data
public class MiembroProyectoDto {
    private Long id;
    @NotNull(groups = {View.Post.class, View.Put.class}, message = "Debe ingresar el id del usuario.")
    @Range(min = 1, groups = {View.Post.class, View.Put.class}, message = "El id del usuario debe ser mayor o igual a 1.")
    private Long usuarioId;
    @NotNull(groups = {View.Post.class, View.Put.class}, message = "Debe ingresar el id del proyecto.")
    @Range(min = 1, groups = {View.Post.class, View.Put.class}, message = "El id del proyecto debe ser mayor o igual a 1.")
    private Long proyectoId;
    private String rolEnProyecto;
}
