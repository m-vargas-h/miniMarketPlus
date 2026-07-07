package com.minimarket.controller;

import com.minimarket.entity.Venta;
import com.minimarket.service.VentaService;
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
@RequestMapping("/api/ventas")
@Tag(name = "Ventas", description = "Gestión de ventas del minimarket — requiere rol ADMIN o EMPLEADO")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Listar todas las ventas",
        description = "Retorna el historial completo de ventas registradas. Requiere rol ADMIN o EMPLEADO."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = Venta.class)))),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN o EMPLEADO", content = @Content)
    })
    public List<Venta> listarVentas() {
        return ventaService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Obtener venta por ID",
        description = "Retorna una venta específica por su ID. Requiere rol ADMIN o EMPLEADO."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Venta encontrada",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Venta.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN o EMPLEADO", content = @Content),
        @ApiResponse(responseCode = "404", description = "No existe una venta con el ID indicado", content = @Content)
    })
    public ResponseEntity<Venta> obtenerVentaPorId(
            @Parameter(description = "ID de la venta a buscar", example = "1", required = true)
            @PathVariable Long id) {
        Venta venta = ventaService.findById(id);
        return (venta != null) ? ResponseEntity.ok(venta) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Registrar venta",
        description = "Crea un nuevo registro de venta en el sistema. Requiere rol ADMIN o EMPLEADO."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Venta registrada correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Venta.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN o EMPLEADO", content = @Content)
    })
    public Venta guardarVenta(@RequestBody Venta venta) {
        return ventaService.save(venta);
    }
}