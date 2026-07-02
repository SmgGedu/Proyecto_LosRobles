package com.losrobles.api.repositories;

import com.losrobles.api.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol WHERE u.username = :username")
    Optional<Usuario> findByUsername(@Param("username") String username);

    Boolean existsByUsername(String username);

    Boolean existsByDni(String dni);

    Long countByRol_Id(Integer rolId);

    @Query("SELECT u FROM Usuario u " +
            "LEFT JOIN FETCH u.departamento d " +
            "WHERE u.rol.id = 3 " +
            "AND (u.nombres LIKE %:filtro% OR u.apellidos LIKE %:filtro%)")
    List<Usuario> buscarResidentePorNombre(@Param("filtro") String filtro);

    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.rol.id = 3")
    Long contarResidentes();
}