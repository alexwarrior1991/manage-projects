package com.alejandro.manageprojects.domain.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.alejandro.manageprojects.view.View;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class PerfilDto {
    @JsonView({View.Get.class})
    private Long id;

    @JsonView({View.Post.class, View.Put.class, View.Get.class})
    private String telefono;

    @JsonView({View.Post.class, View.Put.class, View.Get.class})
    private String direccion;

    @JsonView({View.Post.class, View.Put.class, View.Get.class})
    private String avatarUrl;

    @JsonView({View.Post.class, View.Put.class, View.Get.class})
    @NotNull(groups = {View.Post.class, View.Put.class}, message = "Debe ingresar el usuario del perfil.")
    private UsuarioDto usuario;
}
