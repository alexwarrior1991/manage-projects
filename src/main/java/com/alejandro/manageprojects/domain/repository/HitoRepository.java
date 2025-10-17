package com.alejandro.manageprojects.domain.repository;

import com.alejandro.manageprojects.domain.entity.Hito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface HitoRepository extends JpaRepository<Hito, Long>, QuerydslPredicateExecutor<Hito> {
}
