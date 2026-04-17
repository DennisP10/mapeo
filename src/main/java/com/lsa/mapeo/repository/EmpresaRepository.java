package com.lsa.mapeo.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.lsa.mapeo.model.Empresa;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Integer> {
    // Método necesario para buscar si la empresa ya existe antes de crearla
    Optional<Empresa> findByNombre(String nombre);
}