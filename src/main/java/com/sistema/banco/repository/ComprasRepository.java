package com.sistema.banco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sistema.banco.models.Compras;

@Repository
public interface ComprasRepository extends JpaRepository<Compras, Long> {

}
