package com.losrobles.api.controllers;

import com.losrobles.api.models.Rol;
import com.losrobles.api.services.RolService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolService rolService;

    @GetMapping
    public List<Rol> listar() {
        return rolService.findAll();
    }

    @PostMapping
    public Rol crear(@RequestBody Rol rol) {
        return rolService.save(rol);
    }
}
