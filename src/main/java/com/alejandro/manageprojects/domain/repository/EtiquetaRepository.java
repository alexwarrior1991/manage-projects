package com.alejandro.manageprojects.domain.repository;

import com.alejandro.manageprojects.domain.entity.Etiqueta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface EtiquetaRepository extends JpaRepository<Etiqueta, Long>, QuerydslPredicateExecutor<Etiqueta> {
}
