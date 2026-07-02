package com.losrobles.api.services;

import com.losrobles.api.dto.DepartamentoResponseDTO;
import com.losrobles.api.models.Departamento;
import com.losrobles.api.models.Usuario;
import com.losrobles.api.repositories.DepartamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartamentoService {

    private final DepartamentoRepository departamentoRepository;

    public List<Departamento> findAll() {
        return departamentoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<DepartamentoResponseDTO> findAllConEstado() {
        return departamentoRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Departamento save(Departamento departamento) {
        return departamentoRepository.save(departamento);
    }

    public Optional<Departamento> findById(Integer id) {
        return departamentoRepository.findById(id);
    }

    /**
     * "Da de baja" un departamento desvinculando a su(s) residente(s),
     * dejándolo libre para asignar a otra familia.
     */
    @Transactional
    public DepartamentoResponseDTO liberar(Integer id) {
        Departamento depto = departamentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Error: Departamento no encontrado."));
        if (depto.getResidentes() != null) {
            depto.getResidentes().forEach(r -> r.setDepartamento(null));
            depto.getResidentes().clear();
        }
        return mapToDTO(depto);
    }

    private DepartamentoResponseDTO mapToDTO(Departamento d) {
        String residente = "No tiene";
        String estado = "Libre";
        if (d.getResidentes() != null && !d.getResidentes().isEmpty()) {
            Usuario r = d.getResidentes().get(0);
            residente = r.getNombres() + " " + r.getApellidos();
            estado = "Ocupado";
        }
        return DepartamentoResponseDTO.builder()
                .id(d.getId())
                .bloqueTorre(d.getBloqueTorre())
                .numeroDepa(d.getNumeroDepa())
                .residente(residente)
                .estado(estado)
                .build();
    }
}
