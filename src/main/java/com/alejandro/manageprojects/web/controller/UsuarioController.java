package com.alejandro.manageprojects.web.controller;

import com.alejandro.manageprojects.domain.dto.UsuarioDto;
import com.alejandro.manageprojects.domain.service.UsuarioService;
import com.alejandro.manageprojects.view.View;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @JsonView(View.Post.class)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsuarioDto> create(
            @JsonView(View.Post.class)
            @Validated(View.Post.class) @RequestBody UsuarioDto dto) {
        return ResponseEntity.ok(usuarioService.create(dto));
    }

    @JsonView(View.Put.class)
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsuarioDto> update(
            @PathVariable Long id,
            @JsonView(View.Put.class)
            @Validated(View.Put.class) @RequestBody UsuarioDto dto) {
        return ResponseEntity.ok(usuarioService.update(id, dto));
    }

    @JsonView(View.Get.class)
    @GetMapping(path = "/{id}")
    public ResponseEntity<UsuarioDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.getById(id));
    }

    @JsonView(View.Get.class)
    @GetMapping
    public ResponseEntity<List<UsuarioDto>> getAll() {
        return ResponseEntity.ok(usuarioService.getAll());
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @JsonView(View.Get.class)
    @GetMapping(path = "/search")
    public ResponseEntity<List<UsuarioDto>> search(
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return ResponseEntity.ok(usuarioService.searchByRolAndCreadoEnBetween(rol, desde, hasta));
    }
}
