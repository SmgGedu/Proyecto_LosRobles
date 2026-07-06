package com.losrobles.api.repositories;

import com.losrobles.api.models.Invitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InvitacionRepository extends JpaRepository<Invitacion, Integer> {
Optional<Invitacion> findByCodigoQrHash(String codigoQrHash);

List<Invitacion> findByAnfitrion_UsernameOrderByFechaCreacionDesc(String username);
}