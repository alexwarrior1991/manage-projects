package com.alejandro.manageprojects.domain.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.alejandro.manageprojects.view.View;
import lombok.Data;

import java.util.Set;
import jakarta.validation.constraints.NotBlank;

@Data
public class UsuarioDto {
    @JsonView({View.Get.class})
    private Long id;

    @JsonView({View.Post.class, View.Put.class, View.Get.class})
    @NotBlank(groups = {View.Post.class, View.Put.class}, message = "Debe ingresar el email del usuario.")
    private String email;

    @JsonView({View.Post.class, View.Put.class, View.Get.class})
    @NotBlank(groups = {View.Post.class, View.Put.class}, message = "Debe ingresar el nombre del usuario.")
    private String nombre;

    @JsonView({View.Post.class, View.Put.class, View.Get.class})
    @NotBlank(groups = {View.Post.class, View.Put.class}, message = "Debe ingresar el apellido del usuario.")
    private String apellido;

    @JsonView({View.Post.class, View.Put.class, View.Get.class})
    private PerfilDto perfil;

    @JsonView({View.Post.class, View.Put.class, View.Get.class})
    private Set<RolDto> roles;
}
