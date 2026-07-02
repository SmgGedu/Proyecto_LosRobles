package com.losrobles.api.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Utilidad central para fecha y hora del sistema. Todo el backend debe operar
 * con la zona horaria de Perú (America/Lima, UTC-5), independientemente de la
 * zona horaria del servidor (Azure suele usar UTC).
 */
public final class FechaUtils {

    public static final ZoneId ZONA_PERU = ZoneId.of("America/Lima");

    private FechaUtils() {
    }

    public static LocalDateTime ahora() {
        return LocalDateTime.now(ZONA_PERU);
    }

    public static LocalDate hoy() {
        return LocalDate.now(ZONA_PERU);
    }
}
