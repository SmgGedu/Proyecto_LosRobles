package com.losrobles.api.repositories;

import com.losrobles.api.models.Visitante;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitanteRepository extends JpaRepository<Visitante, String> {
    Optional<Visitante> findByDni(String dni);
}