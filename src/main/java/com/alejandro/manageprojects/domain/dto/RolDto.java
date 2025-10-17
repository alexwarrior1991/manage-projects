package com.alejandro.manageprojects.domain.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import com.alejandro.manageprojects.view.View;

@Data
public class RolDto {
    private Long id;
    @NotBlank(groups = {View.Post.class, View.Put.class}, message = "Debe ingresar el nombre del rol.")
    private String nombre;
}
