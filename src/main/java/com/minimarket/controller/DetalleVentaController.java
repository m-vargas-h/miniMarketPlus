package com.minimarket.controller;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.service.DetalleVentaService;
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
@RequestMapping("/api/detalle-ventas")
@Tag(name = "Detalle de Ventas", description = "Gestión del detalle de ventas — requiere rol ADMIN o EMPLEADO. Eliminación solo ADMIN.")
public class DetalleVentaController {

    @Autowired
    private DetalleVentaService detalleVentaService;

    // Arma un EntityModel<DetalleVenta> con sus enlaces HATEOAS
    private EntityModel<DetalleVenta> toModel(DetalleVenta detalleVenta) {
        EntityModel<DetalleVenta> model = EntityModel.of(detalleVenta);
        model.add(linkTo(methodOn(DetalleVentaController.class).obtenerDetalleVentaPorId(detalleVenta.getId())).withSelfRel());
        model.add(linkTo(methodOn(DetalleVentaController.class).listarDetalleVentas()).withRel("detalle-ventas"));
        if (detalleVenta.getVenta() != null) {
            model.add(linkTo(methodOn(DetalleVentaController.class).listarDetalleVentasPorVenta(detalleVenta.getVenta().getId()))
                    .withRel("detalles-venta"));
            model.add(linkTo(methodOn(VentaController.class).obtenerVentaPorId(detalleVenta.getVenta().getId()))
                    .withRel("venta"));
        }
        if (detalleVenta.getProducto() != null) {
            model.add(linkTo(methodOn(ProductoController.class).obtenerProductoPorId(detalleVenta.getProducto().getId()))
                    .withRel("producto"));
        }
        return model;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Listar todos los detalles de venta",
        description = "Retorna la lista completa de líneas de detalle asociadas a ventas, con enlaces HATEOAS. Requiere rol ADMIN o EMPLEADO."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = DetalleVenta.class)))),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN o EMPLEADO", content = @Content)
    })
    public CollectionModel<EntityModel<DetalleVenta>> listarDetalleVentas() {
        List<EntityModel<DetalleVenta>> detalles = detalleVentaService.findAll().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(detalles,
                linkTo(methodOn(DetalleVentaController.class).listarDetalleVentas()).withSelfRel());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Obtener detalle de venta por ID",
        description = "Retorna una línea de detalle específica por su ID, con enlaces HATEOAS. Requiere rol ADMIN o EMPLEADO."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detalle de venta encontrado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = DetalleVenta.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN o EMPLEADO", content = @Content),
        @ApiResponse(responseCode = "404", description = "No existe un detalle de venta con el ID indicado", content = @Content)
    })
    public ResponseEntity<EntityModel<DetalleVenta>> obtenerDetalleVentaPorId(
            @Parameter(description = "ID del detalle de venta a buscar", example = "1", required = true)
            @PathVariable Long id) {
        DetalleVenta detalleVenta = detalleVentaService.findById(id);
        if (detalleVenta == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toModel(detalleVenta));
    }

    @GetMapping("/venta/{ventaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Listar detalles por venta",
        description = "Retorna todas las líneas de detalle asociadas a una venta específica, con enlaces HATEOAS. Requiere rol ADMIN o EMPLEADO."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = DetalleVenta.class)))),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN o EMPLEADO", content = @Content)
    })
    public CollectionModel<EntityModel<DetalleVenta>> listarDetalleVentasPorVenta(
            @Parameter(description = "ID de la venta", example = "1", required = true)
            @PathVariable Long ventaId) {
        List<EntityModel<DetalleVenta>> detalles = detalleVentaService.findByVentaId(ventaId).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(detalles,
                linkTo(methodOn(DetalleVentaController.class).listarDetalleVentasPorVenta(ventaId)).withSelfRel(),
                linkTo(methodOn(VentaController.class).obtenerVentaPorId(ventaId)).withRel("venta"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Registrar detalle de venta",
        description = "Crea una nueva línea de detalle asociada a una venta existente. Requiere rol ADMIN o EMPLEADO."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detalle de venta registrado correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = DetalleVenta.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN o EMPLEADO", content = @Content)
    })
    public EntityModel<DetalleVenta> guardarDetalleVenta(@RequestBody DetalleVenta detalleVenta) {
        return toModel(detalleVentaService.save(detalleVenta));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Actualizar detalle de venta",
        description = "Modifica los datos de una línea de detalle existente. Requiere rol ADMIN o EMPLEADO."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detalle de venta actualizado correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = DetalleVenta.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN o EMPLEADO", content = @Content),
        @ApiResponse(responseCode = "404", description = "No existe un detalle de venta con el ID indicado", content = @Content)
    })
    public ResponseEntity<EntityModel<DetalleVenta>> actualizarDetalleVenta(
            @Parameter(description = "ID del detalle de venta a actualizar", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody DetalleVenta detalleVenta) {
        DetalleVenta existente = detalleVentaService.findById(id);
        if (existente != null) {
            detalleVenta.setId(id);
            return ResponseEntity.ok(toModel(detalleVentaService.save(detalleVenta)));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Eliminar detalle de venta",
        description = "Elimina una línea de detalle por su ID. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Detalle de venta eliminado correctamente", content = @Content),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN", content = @Content),
        @ApiResponse(responseCode = "404", description = "No existe un detalle de venta con el ID indicado", content = @Content)
    })
    public ResponseEntity<Void> eliminarDetalleVenta(
            @Parameter(description = "ID del detalle de venta a eliminar", example = "1", required = true)
            @PathVariable Long id) {
        DetalleVenta detalleVenta = detalleVentaService.findById(id);
        if (detalleVenta != null) {
            detalleVentaService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}