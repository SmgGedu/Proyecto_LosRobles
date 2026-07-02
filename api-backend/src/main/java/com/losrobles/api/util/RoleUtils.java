package com.losrobles.api.util;

public final class RoleUtils {

    private RoleUtils() {
    }

    /**
     * Normaliza el nombre de un rol a "Capitalizado" (ej. "conserje" -> "Conserje",
     * "ADMINISTRADOR" -> "Administrador") para que coincida con la convención usada
     * en las anotaciones @PreAuthorize, sin importar cómo esté guardado en la base de datos.
     */
    public static String normalize(String nombreRol) {
        if (nombreRol == null) {
            return null;
        }
        String trimmed = nombreRol.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1).toLowerCase();
    }
}
