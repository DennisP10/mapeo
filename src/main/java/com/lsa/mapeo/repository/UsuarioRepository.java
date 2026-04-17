package com.lsa.mapeo.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.lsa.mapeo.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    // Esta función buscará al usuario por su correo exacto
    Optional<Usuario> findByCorreo(String correo);
}