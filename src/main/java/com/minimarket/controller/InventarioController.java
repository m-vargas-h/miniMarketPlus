package com.minimarket.controller;

import com.minimarket.entity.Inventario;
import com.minimarket.service.InventarioService;
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
@RequestMapping("/api/inventario")
@Tag(name = "Inventario", description = "Gestión de movimientos de inventario — requiere rol ADMIN o EMPLEADO. Eliminación solo ADMIN.")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    // Arma un EntityModel<Inventario> con sus enlaces HATEOAS
    private EntityModel<Inventario> toModel(Inventario inventario) {
        EntityModel<Inventario> model = EntityModel.of(inventario);
        model.add(linkTo(methodOn(InventarioController.class).obtenerMovimientoPorId(inventario.getId())).withSelfRel());
        model.add(linkTo(methodOn(InventarioController.class).listarMovimientosDeInventario()).withRel("inventario"));
        if (inventario.getProducto() != null) {
            model.add(linkTo(methodOn(InventarioController.class).listarMovimientosPorProducto(inventario.getProducto().getId()))
                    .withRel("movimientos-producto"));
            model.add(linkTo(methodOn(ProductoController.class).obtenerProductoPorId(inventario.getProducto().getId()))
                    .withRel("producto"));
        }
        return model;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Listar movimientos de inventario",
        description = "Retorna todos los movimientos de entrada y salida registrados en el inventario, con enlaces HATEOAS. Requiere rol ADMIN o EMPLEADO."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = Inventario.class)))),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN o EMPLEADO", content = @Content)
    })
    public CollectionModel<EntityModel<Inventario>> listarMovimientosDeInventario() {
        List<EntityModel<Inventario>> movimientos = inventarioService.findAll().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(movimientos,
                linkTo(methodOn(InventarioController.class).listarMovimientosDeInventario()).withSelfRel());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Obtener movimiento por ID",
        description = "Retorna un movimiento de inventario específico por su ID, con enlaces HATEOAS. Requiere rol ADMIN o EMPLEADO."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Movimiento encontrado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Inventario.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN o EMPLEADO", content = @Content),
        @ApiResponse(responseCode = "404", description = "No existe un movimiento con el ID indicado", content = @Content)
    })
    public ResponseEntity<EntityModel<Inventario>> obtenerMovimientoPorId(
            @Parameter(description = "ID del movimiento a buscar", example = "1", required = true)
            @PathVariable Long id) {
        Inventario inventario = inventarioService.findById(id);
        if (inventario == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toModel(inventario));
    }

    @GetMapping("/producto/{productoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Listar movimientos por producto",
        description = "Retorna todos los movimientos de inventario asociados a un producto específico, con enlaces HATEOAS. Requiere rol ADMIN o EMPLEADO."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = Inventario.class)))),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN o EMPLEADO", content = @Content)
    })
    public CollectionModel<EntityModel<Inventario>> listarMovimientosPorProducto(
            @Parameter(description = "ID del producto", example = "1", required = true)
            @PathVariable Long productoId) {
        List<EntityModel<Inventario>> movimientos = inventarioService.findByProductoId(productoId).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(movimientos,
                linkTo(methodOn(InventarioController.class).listarMovimientosPorProducto(productoId)).withSelfRel(),
                linkTo(methodOn(ProductoController.class).obtenerProductoPorId(productoId)).withRel("producto"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Registrar movimiento de inventario",
        description = "Registra un nuevo movimiento de entrada o salida de stock. Requiere rol ADMIN o EMPLEADO."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Movimiento registrado correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Inventario.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN o EMPLEADO", content = @Content)
    })
    public EntityModel<Inventario> registrarMovimiento(@RequestBody Inventario inventario) {
        return toModel(inventarioService.save(inventario));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Actualizar movimiento de inventario",
        description = "Modifica los datos de un movimiento de inventario existente. Requiere rol ADMIN o EMPLEADO."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Movimiento actualizado correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Inventario.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN o EMPLEADO", content = @Content),
        @ApiResponse(responseCode = "404", description = "No existe un movimiento con el ID indicado", content = @Content)
    })
    public ResponseEntity<EntityModel<Inventario>> actualizarMovimiento(
            @Parameter(description = "ID del movimiento a actualizar", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody Inventario inventario) {
        Inventario existente = inventarioService.findById(id);
        if (existente != null) {
            inventario.setId(id);
            return ResponseEntity.ok(toModel(inventarioService.save(inventario)));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Eliminar movimiento de inventario",
        description = "Elimina un movimiento de inventario por su ID. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Movimiento eliminado correctamente", content = @Content),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN", content = @Content),
        @ApiResponse(responseCode = "404", description = "No existe un movimiento con el ID indicado", content = @Content)
    })
    public ResponseEntity<Void> eliminarMovimiento(
            @Parameter(description = "ID del movimiento a eliminar", example = "1", required = true)
            @PathVariable Long id) {
        Inventario inventario = inventarioService.findById(id);
        if (inventario != null) {
            inventarioService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}