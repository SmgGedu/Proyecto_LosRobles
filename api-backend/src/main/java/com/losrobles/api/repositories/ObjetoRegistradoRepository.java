package com.losrobles.api.repositories;

import com.losrobles.api.models.ObjetoRegistrado;
import com.losrobles.api.models.RegistroAcceso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ObjetoRegistradoRepository extends JpaRepository<ObjetoRegistrado, Integer> {
    List<ObjetoRegistrado> findByRegistroAcceso(RegistroAcceso registroAcceso);
}