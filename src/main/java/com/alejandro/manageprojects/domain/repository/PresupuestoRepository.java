package com.alejandro.manageprojects.domain.repository;

import com.alejandro.manageprojects.domain.entity.Presupuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface PresupuestoRepository extends JpaRepository<Presupuesto, Long>, QuerydslPredicateExecutor<Presupuesto> {
}
