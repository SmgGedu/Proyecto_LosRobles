package com.losrobles.api.repositories;

import com.losrobles.api.models.EventoSeguridad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoSeguridadRepository extends JpaRepository<EventoSeguridad, Integer> {
}
