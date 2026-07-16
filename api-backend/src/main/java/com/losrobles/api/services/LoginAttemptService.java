package com.losrobles.api.services;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Control de fuerza bruta en el login: bloquea temporalmente un username
 * tras varios intentos fallidos consecutivos. Estado en memoria (por
 * instancia); suficiente para una sola instancia de backend como la de este
 * proyecto.
 */
@Service
public class LoginAttemptService {

    private static final int MAX_INTENTOS = 5;
    private static final Duration BLOQUEO = Duration.ofMinutes(15);

    private record Intentos(AtomicInteger fallos, Instant bloqueadoHasta) {
    }

    private final ConcurrentHashMap<String, Intentos> porUsuario = new ConcurrentHashMap<>();

    /** Segundos restantes de bloqueo, o 0 si el usuario puede intentar login. */
    public long segundosDeBloqueoRestante(String username) {
        Intentos intentos = porUsuario.get(normalizar(username));
        if (intentos == null || intentos.bloqueadoHasta() == null) {
            return 0;
        }
        long restante = Duration.between(Instant.now(), intentos.bloqueadoHasta()).getSeconds();
        return Math.max(restante, 0);
    }

    public void registrarFallo(String username) {
        String key = normalizar(username);
        Intentos intentos = porUsuario.computeIfAbsent(key, k -> new Intentos(new AtomicInteger(0), null));
        int fallos = intentos.fallos().incrementAndGet();
        if (fallos >= MAX_INTENTOS) {
            porUsuario.put(key, new Intentos(intentos.fallos(), Instant.now().plus(BLOQUEO)));
        }
    }

    public void registrarExito(String username) {
        porUsuario.remove(normalizar(username));
    }

    private String normalizar(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }
}
