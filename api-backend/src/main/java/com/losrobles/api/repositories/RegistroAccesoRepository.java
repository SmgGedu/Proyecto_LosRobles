package com.losrobles.api.repositories;

import com.losrobles.api.models.RegistroAcceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface RegistroAccesoRepository extends JpaRepository<RegistroAcceso, Integer> {

    @Query("SELECT r FROM RegistroAcceso r WHERE r.horaIngreso BETWEEN :desde AND :hasta " +
            "AND (:tipoVisita IS NULL OR r.tipoVisita = :tipoVisita) " +
            "ORDER BY r.horaIngreso DESC")
    List<RegistroAcceso> findForReporte(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta,
            @Param("tipoVisita") String tipoVisita);
    List<RegistroAcceso> findByEstadoAcceso(String estado);

    long countByEstadoAcceso(String estado);

    // Cuenta cuántos entraron hoy (fecha calculada en hora de Perú, no GETDATE() que es UTC en Azure)
    @Query(value = "SELECT COUNT(*) FROM tbl_registro_accesos WHERE CAST(hora_ingreso AS DATE) = :hoy", nativeQuery = true)
    long countVisitantesHoy(@Param("hoy") LocalDate hoy);

    // Cuenta los que no tienen fecha de salida (siguen dentro)
    @Query("SELECT COUNT(r) FROM RegistroAcceso r WHERE r.horaSalida IS NULL")
    long countActivosAhora();

    // Obtiene la hora del último registro
    @Query(value = "SELECT TOP 1 FORMAT(hora_ingreso, 'hh:mm tt') FROM tbl_registro_accesos ORDER BY hora_ingreso DESC", nativeQuery = true)
    String findHoraUltimoIngreso();

}