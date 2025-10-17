package com.alejandro.manageprojects.domain.repository;

import com.alejandro.manageprojects.domain.entity.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface PerfilRepository extends JpaRepository<Perfil, Long>, QuerydslPredicateExecutor<Perfil> {
}
