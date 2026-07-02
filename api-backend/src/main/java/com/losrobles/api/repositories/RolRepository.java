package com.losrobles.api.repositories;

import com.losrobles.api.models.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepository extends JpaRepository<Rol, Integer> {
    // Esto nos da métodos como save(), findAll(), findById() automáticamente[cite: 161].
}