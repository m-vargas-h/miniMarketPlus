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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/detalle-ventas")
@Tag(name = "Detalle de Ventas", description = "Gestión del detalle de ventas — requiere rol ADMIN o EMPLEADO. Eliminación solo ADMIN.")
public class DetalleVentaController {

    @Autowired
    private DetalleVentaService detalleVentaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Listar todos los detalles de venta",
        description = "Retorna la lista completa de líneas de detalle asociadas a ventas. Requiere rol ADMIN o EMPLEADO."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = DetalleVenta.class)))),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN o EMPLEADO", content = @Content)
    })
    public List<DetalleVenta> listarDetalleVentas() {
        return detalleVentaService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Obtener detalle de venta por ID",
        description = "Retorna una línea de detalle específica por su ID. Requiere rol ADMIN o EMPLEADO."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detalle de venta encontrado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = DetalleVenta.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN o EMPLEADO", content = @Content),
        @ApiResponse(responseCode = "404", description = "No existe un detalle de venta con el ID indicado", content = @Content)
    })
    public ResponseEntity<DetalleVenta> obtenerDetalleVentaPorId(
            @Parameter(description = "ID del detalle de venta a buscar", example = "1", required = true)
            @PathVariable Long id) {
        DetalleVenta detalleVenta = detalleVentaService.findById(id);
        return (detalleVenta != null) ? ResponseEntity.ok(detalleVenta) : ResponseEntity.notFound().build();
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
    public DetalleVenta guardarDetalleVenta(@RequestBody DetalleVenta detalleVenta) {
        return detalleVentaService.save(detalleVenta);
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
    public ResponseEntity<DetalleVenta> actualizarDetalleVenta(
            @Parameter(description = "ID del detalle de venta a actualizar", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody DetalleVenta detalleVenta) {
        DetalleVenta existente = detalleVentaService.findById(id);
        if (existente != null) {
            detalleVenta.setId(id);
            return ResponseEntity.ok(detalleVentaService.save(detalleVenta));
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