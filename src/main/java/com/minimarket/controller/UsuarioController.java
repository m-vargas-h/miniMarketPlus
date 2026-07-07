package com.minimarket.controller;

import com.minimarket.entity.Usuario;
import com.minimarket.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
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
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = Usuario.class)))),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN", content = @Content)
    })
    public List<Usuario> listarUsuarios() {
        return usuarioService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener usuario por ID",
        description = "Retorna un usuario específico por su ID. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario encontrado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Usuario.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN", content = @Content),
        @ApiResponse(responseCode = "404", description = "No existe un usuario con el ID indicado", content = @Content)
    })
    public ResponseEntity<Usuario> obtenerUsuarioPorId(
            @Parameter(description = "ID del usuario a buscar", example = "1", required = true)
            @PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.findById(id);
        return usuario.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(
        summary = "Crear usuario",
        description = "Registra un nuevo usuario en el sistema. La contraseña se almacena encriptada con BCrypt. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario creado correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Usuario.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN", content = @Content)
    })
    public Usuario guardarUsuario(@RequestBody Usuario usuario) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioService.save(usuario);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar usuario",
        description = "Modifica los datos de un usuario existente. La contraseña se re-encripta automáticamente si es modificada. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Usuario.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN", content = @Content),
        @ApiResponse(responseCode = "404", description = "No existe un usuario con el ID indicado", content = @Content)
    })
    public ResponseEntity<Usuario> actualizarUsuario(
            @Parameter(description = "ID del usuario a actualizar", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody Usuario usuario) {
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
        description = "Elimina un usuario del sistema por su ID. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Usuario eliminado correctamente", content = @Content),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN", content = @Content),
        @ApiResponse(responseCode = "404", description = "No existe un usuario con el ID indicado", content = @Content)
    })
    public ResponseEntity<Void> eliminarUsuario(
            @Parameter(description = "ID del usuario a eliminar", example = "1", required = true)
            @PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.findById(id);
        if (usuario.isPresent()) {
            usuarioService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}