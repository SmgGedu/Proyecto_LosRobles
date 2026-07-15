package com.losrobles.api.repositories;

import com.losrobles.api.models.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Integer> {
    // Esto nos da métodos como save(), findAll(), findById() automáticamente[cite: 161].

    Optional<Rol> findByNombreRolIgnoreCase(String nombreRol);
}