package com.minimarket.controller;

import com.minimarket.entity.Usuario;
import com.minimarket.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema — acceso exclusivo para rol ADMIN")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    @Operation(
        summary = "Listar todos los usuarios",
        description = "Retorna la lista completa de usuarios registrados en el sistema. Requiere rol ADMIN."
    )
    public List<Usuario> listarUsuarios() {
        return usuarioService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener usuario por ID",
        description = "Retorna un usuario específico por su ID. Requiere rol ADMIN. Retorna 404 si no existe."
    )
    public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.findById(id);
        return usuario.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(
        summary = "Crear usuario",
        description = "Registra un nuevo usuario en el sistema. La contraseña se almacena encriptada con BCrypt. Requiere rol ADMIN."
    )
    public Usuario guardarUsuario(@RequestBody Usuario usuario) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioService.save(usuario);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar usuario",
        description = "Modifica los datos de un usuario existente. La contraseña se re-encripta automáticamente si es modificada. Requiere rol ADMIN. Retorna 404 si no existe."
    )
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Long id, @RequestBody Usuario usuario) {
        Optional<Usuario> usuarioExistente = usuarioService.findById(id);
        if (usuarioExistente.isPresent()) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            usuario.setId(id);
            return ResponseEntity.ok(usuarioService.save(usuario));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar usuario",
        description = "Elimina un usuario del sistema por su ID. Requiere rol ADMIN. Retorna 204 si se eliminó correctamente, 404 si no existe."
    )
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.findById(id);
        if (usuario.isPresent()) {
            usuarioService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}