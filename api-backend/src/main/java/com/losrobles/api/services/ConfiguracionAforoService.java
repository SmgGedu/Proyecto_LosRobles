package com.losrobles.api.services;

import com.losrobles.api.models.ConfiguracionAforo;
import com.losrobles.api.repositories.ConfiguracionAforoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Gestiona los parámetros de aforo del condominio (clave-valor), con valores
 * por defecto cuando aún no se ha guardado ninguna configuración.
 */
@Service
@RequiredArgsConstructor
public class ConfiguracionAforoService {

    private static final String CLAVE_AFORO_MAXIMO = "AFORO_MAXIMO";
    private static final String CLAVE_TIEMPO_MAXIMO_VISITA = "TIEMPO_MAXIMO_VISITA_MINUTOS";

    private static final int DEFAULT_AFORO_MAXIMO = 50;
    private static final int DEFAULT_TIEMPO_MAXIMO_VISITA = 240;

    private final ConfiguracionAforoRepository configuracionRepo;

    public int obtenerAforoMaximo() {
        return obtenerValor(CLAVE_AFORO_MAXIMO, DEFAULT_AFORO_MAXIMO);
    }

    public int obtenerTiempoMaximoVisita() {
        return obtenerValor(CLAVE_TIEMPO_MAXIMO_VISITA, DEFAULT_TIEMPO_MAXIMO_VISITA);
    }

    public void actualizarAforoMaximo(int valor) {
        actualizarValor(CLAVE_AFORO_MAXIMO, valor);
    }

    public void actualizarTiempoMaximoVisita(int valor) {
        actualizarValor(CLAVE_TIEMPO_MAXIMO_VISITA, valor);
    }

    private int obtenerValor(String clave, int valorPorDefecto) {
        return configuracionRepo.findByClave(clave)
                .map(config -> {
                    try {
                        return Integer.parseInt(config.getValor());
                    } catch (NumberFormatException e) {
                        return valorPorDefecto;
                    }
                })
                .orElse(valorPorDefecto);
    }

    private void actualizarValor(String clave, int valor) {
        ConfiguracionAforo config = configuracionRepo.findByClave(clave)
                .orElseGet(() -> {
                    ConfiguracionAforo nueva = new ConfiguracionAforo();
                    nueva.setClave(clave);
                    return nueva;
                });
        config.setValor(String.valueOf(valor));
        configuracionRepo.save(config);
    }
}
