package com.losrobles.api.repositories;

import com.losrobles.api.models.ConfiguracionAforo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfiguracionAforoRepository extends JpaRepository<ConfiguracionAforo, Integer> {
    Optional<ConfiguracionAforo> findByClave(String clave);
}
