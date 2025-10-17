package com.alejandro.manageprojects.domain.repository;

import com.alejandro.manageprojects.domain.entity.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface ProyectoRepository extends JpaRepository<Proyecto, Long>, QuerydslPredicateExecutor<Proyecto> {
}
