package com.alejandro.manageprojects.domain.repository;

import com.alejandro.manageprojects.domain.entity.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface TareaRepository extends JpaRepository<Tarea, Long>, QuerydslPredicateExecutor<Tarea> {
}
