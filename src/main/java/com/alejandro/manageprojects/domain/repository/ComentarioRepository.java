package com.alejandro.manageprojects.domain.repository;

import com.alejandro.manageprojects.domain.entity.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface ComentarioRepository extends JpaRepository<Comentario, Long>, QuerydslPredicateExecutor<Comentario> {
}
