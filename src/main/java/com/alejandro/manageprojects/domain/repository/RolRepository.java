package com.alejandro.manageprojects.domain.repository;

import com.alejandro.manageprojects.domain.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface RolRepository extends JpaRepository<Rol, Long>, QuerydslPredicateExecutor<Rol> {
}
