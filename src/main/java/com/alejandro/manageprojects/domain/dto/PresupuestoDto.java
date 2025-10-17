package com.alejandro.manageprojects.domain.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.alejandro.manageprojects.view.View;
import lombok.Data;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;

@Data
public class PresupuestoDto {
    @JsonView({View.Get.class})
    private Long id;

    @JsonView({View.Post.class, View.Put.class, View.Get.class})
    @NotNull(groups = {View.Post.class, View.Put.class}, message = "Debe ingresar el monto total del presupuesto.")
    private BigDecimal montoTotal;

    @JsonView({View.Post.class, View.Put.class, View.Get.class})
    private BigDecimal gastado;

    @JsonView({View.Post.class, View.Put.class, View.Get.class})
    @NotNull(groups = {View.Post.class, View.Put.class}, message = "Debe ingresar el proyecto asociado al presupuesto.")
    private ProyectoDto proyecto;
}
