package com.losrobles.api.services;

import com.losrobles.api.dto.DashboardStatsDTO;
import com.losrobles.api.repositories.RegistroAccesoRepository;
import com.losrobles.api.repositories.UsuarioRepository;
import com.losrobles.api.util.FechaUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final RegistroAccesoRepository accesoRepository;

    private final UsuarioRepository usuarioRepository;

    public DashboardStatsDTO getStats() {
        Long hoy = accesoRepository.countVisitantesHoy(FechaUtils.hoy());
        Long activos = accesoRepository.countActivosAhora();
        Long residentes = usuarioRepository.contarResidentes();
        String ultimo = accesoRepository.findHoraUltimoIngreso();
        if (ultimo == null) {
            ultimo = "--:--";
        }
        return new DashboardStatsDTO(hoy, activos, residentes, ultimo);
    }
}
