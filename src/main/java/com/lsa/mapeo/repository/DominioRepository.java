package com.lsa.mapeo.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.lsa.mapeo.model.Dominio;

@Repository
public interface DominioRepository extends JpaRepository<Dominio, Integer> {
    // Método necesario para buscar el endpoint de Chatwoot
    Optional<Dominio> findByUrlDominio(String urlDominio);
}