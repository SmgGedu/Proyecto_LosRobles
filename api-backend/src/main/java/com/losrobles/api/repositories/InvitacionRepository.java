package com.losrobles.api.repositories;

import com.losrobles.api.models.Invitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InvitacionRepository extends JpaRepository<Invitacion, Integer> {
Optional<Invitacion> findByCodigoQrHash(String codigoQrHash);
}