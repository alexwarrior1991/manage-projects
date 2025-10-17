package com.alejandro.manageprojects.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "presupuestos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Presupuesto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal montoTotal;

    @Column(precision = 19, scale = 2)
    private BigDecimal gastado;

    @OneToOne
    @JoinColumn(name = "proyecto_id", nullable = false, unique = true)
    private Proyecto proyecto;
}
