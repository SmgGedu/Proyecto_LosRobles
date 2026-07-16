package com.losrobles.api.controllers;

import com.losrobles.api.models.Rol;
import com.losrobles.api.services.RolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Gestión de roles: uso exclusivo del panel de administración (alta de
// usuarios). Sin restricción, cualquier usuario autenticado (p.ej. un
// Residente) podía listar y crear roles arbitrarios en el sistema.
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_Administrador')")
public class RolController {

    private final RolService rolService;

    @GetMapping
    public List<Rol> listar() {
        return rolService.findAll();
    }

    @PostMapping
    public Rol crear(@Valid @RequestBody Rol rol) {
        return rolService.save(rol);
    }
}
