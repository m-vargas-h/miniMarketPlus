package com.minimarket.controller;

import com.minimarket.entity.Carrito;
import com.minimarket.service.CarritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carrito")
@Tag(name = "Carrito", description = "Gestión del carrito de compras — requiere rol ADMIN o CLIENTE")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Listar items del carrito",
        description = "Retorna todos los productos agregados al carrito."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = Carrito.class)))),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN o CLIENTE", content = @Content)
    })
    public List<Carrito> listarCarrito() {
        return carritoService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Obtener item del carrito por ID",
        description = "Retorna un item específico del carrito."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Item encontrado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Carrito.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN o CLIENTE", content = @Content),
        @ApiResponse(responseCode = "404", description = "No existe un item de carrito con el ID indicado", content = @Content)
    })
    public ResponseEntity<Carrito> obtenerCarritoPorId(
            @Parameter(description = "ID del item de carrito a buscar", example = "1", required = true)
            @PathVariable Long id) {
        Carrito carrito = carritoService.findById(id);
        return (carrito != null) ? ResponseEntity.ok(carrito) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Agregar producto al carrito",
        description = "Agrega un nuevo producto al carrito de compras."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto agregado correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Carrito.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN o CLIENTE", content = @Content)
    })
    public Carrito agregarProductoAlCarrito(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Datos del item a agregar al carrito. Basta con el ID de usuario y producto.",
                required = true,
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(name = "Nuevo item de carrito",
                        value = "{\"usuario\": {\"id\": 1}, \"producto\": {\"id\": 2}, \"cantidad\": 3}")))
            @RequestBody Carrito carrito) {
        return carritoService.save(carrito);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Actualizar item del carrito",
        description = "Modifica la cantidad u otros datos de un producto en el carrito."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Item actualizado correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Carrito.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN o CLIENTE", content = @Content),
        @ApiResponse(responseCode = "404", description = "No existe un item de carrito con el ID indicado", content = @Content)
    })
    public ResponseEntity<Carrito> actualizarCarrito(
            @Parameter(description = "ID del item de carrito a actualizar", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody Carrito carrito) {
        Carrito existente = carritoService.findById(id);
        if (existente != null) {
            carrito.setId(id);
            return ResponseEntity.ok(carritoService.save(carrito));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Eliminar producto del carrito",
        description = "Elimina un producto del carrito por su ID."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Item eliminado correctamente", content = @Content),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN o CLIENTE", content = @Content),
        @ApiResponse(responseCode = "404", description = "No existe un item de carrito con el ID indicado", content = @Content)
    })
    public ResponseEntity<Void> eliminarProductoDelCarrito(
            @Parameter(description = "ID del item de carrito a eliminar", example = "1", required = true)
            @PathVariable Long id) {
        Carrito carrito = carritoService.findById(id);
        if (carrito != null) {
            carritoService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}