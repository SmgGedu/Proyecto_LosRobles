package com.losrobles.api.services;

import com.losrobles.api.models.Visitante;
import com.losrobles.api.repositories.VisitanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VisitanteService {

    private final VisitanteRepository visitanteRepository;

    public List<Visitante> listarTodos() {
        return visitanteRepository.findAll();
    }

    public Visitante guardar(Visitante visitante) {
        return visitanteRepository.save(visitante);
    }

    public Optional<Visitante> buscarPorDni(String dni) {
        return visitanteRepository.findByDni(dni);
    }
}
