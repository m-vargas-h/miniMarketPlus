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
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/carrito")
@Tag(name = "Carrito", description = "Gestión del carrito de compras — requiere rol ADMIN o CLIENTE")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    // Arma un EntityModel<Carrito> con sus enlaces HATEOAS
    private EntityModel<Carrito> toModel(Carrito carrito) {
        EntityModel<Carrito> model = EntityModel.of(carrito);
        model.add(linkTo(methodOn(CarritoController.class).obtenerCarritoPorId(carrito.getId())).withSelfRel());
        model.add(linkTo(methodOn(CarritoController.class).listarCarrito()).withRel("carritos"));
        if (carrito.getUsuario() != null) {
            model.add(linkTo(methodOn(CarritoController.class).listarCarritoPorUsuario(carrito.getUsuario().getId()))
                    .withRel("carrito-usuario"));
            model.add(linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(carrito.getUsuario().getId()))
                    .withRel("usuario"));
        }
        if (carrito.getProducto() != null) {
            model.add(linkTo(methodOn(ProductoController.class).obtenerProductoPorId(carrito.getProducto().getId()))
                    .withRel("producto"));
        }
        return model;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Listar items del carrito",
        description = "Retorna todos los productos agregados al carrito, con enlaces HATEOAS."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = Carrito.class)))),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN o CLIENTE", content = @Content)
    })
    public CollectionModel<EntityModel<Carrito>> listarCarrito() {
        List<EntityModel<Carrito>> items = carritoService.findAll().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(items,
                linkTo(methodOn(CarritoController.class).listarCarrito()).withSelfRel());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Obtener item del carrito por ID",
        description = "Retorna un item específico del carrito, con enlaces HATEOAS."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Item encontrado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Carrito.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN o CLIENTE", content = @Content),
        @ApiResponse(responseCode = "404", description = "No existe un item de carrito con el ID indicado", content = @Content)
    })
    public EntityModel<Carrito> obtenerCarritoPorId(
            @Parameter(description = "ID del item de carrito a buscar", example = "1", required = true)
            @PathVariable Long id) {
        Carrito carrito = carritoService.findById(id);
        if (carrito == null) {
            return null;
        }
        return toModel(carrito);
    }

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Listar carrito por usuario",
        description = "Retorna todos los items de carrito asociados a un usuario específico, con enlaces HATEOAS."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = Carrito.class)))),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN o CLIENTE", content = @Content)
    })
    public CollectionModel<EntityModel<Carrito>> listarCarritoPorUsuario(
            @Parameter(description = "ID del usuario", example = "1", required = true)
            @PathVariable Long usuarioId) {
        List<EntityModel<Carrito>> items = carritoService.findByUsuarioId(usuarioId).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(items,
                linkTo(methodOn(CarritoController.class).listarCarritoPorUsuario(usuarioId)).withSelfRel(),
                linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(usuarioId)).withRel("usuario"));
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
    public EntityModel<Carrito> agregarProductoAlCarrito(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Datos del item a agregar al carrito. Basta con el ID de usuario y producto.",
                required = true,
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(name = "Nuevo item de carrito",
                        value = "{\"usuario\": {\"id\": 1}, \"producto\": {\"id\": 2}, \"cantidad\": 3}")))
            @RequestBody Carrito carrito) {
        return toModel(carritoService.save(carrito));
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
    public ResponseEntity<EntityModel<Carrito>> actualizarCarrito(
            @Parameter(description = "ID del item de carrito a actualizar", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody Carrito carrito) {
        Carrito existente = carritoService.findById(id);
        if (existente != null) {
            carrito.setId(id);
            return ResponseEntity.ok(toModel(carritoService.save(carrito)));
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