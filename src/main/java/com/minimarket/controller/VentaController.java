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
@RequestMapping("/api/ventas")
@Tag(name = "Ventas", description = "Gestión de ventas del minimarket — requiere rol ADMIN o EMPLEADO")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    // --- Helper: arma un EntityModel<Venta> con sus enlaces HATEOAS ---
    private EntityModel<Venta> toModel(Venta venta) {
        EntityModel<Venta> model = EntityModel.of(venta);
        model.add(linkTo(methodOn(VentaController.class).obtenerVentaPorId(venta.getId())).withSelfRel());
        model.add(linkTo(methodOn(VentaController.class).listarVentas()).withRel("ventas"));
        if (venta.getUsuario() != null) {
            model.add(linkTo(methodOn(VentaController.class).listarVentasPorUsuario(venta.getUsuario().getId()))
                    .withRel("ventas-usuario"));
            model.add(linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(venta.getUsuario().getId()))
                    .withRel("usuario"));
        }
        model.add(linkTo(methodOn(DetalleVentaController.class).listarDetalleVentasPorVenta(venta.getId()))
                .withRel("detalles"));
        return model;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Listar todas las ventas",
        description = "Retorna el historial completo de ventas registradas, con enlaces HATEOAS. Requiere rol ADMIN o EMPLEADO."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = Venta.class)))),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN o EMPLEADO", content = @Content)
    })
    public CollectionModel<EntityModel<Venta>> listarVentas() {
        List<EntityModel<Venta>> ventas = ventaService.findAll().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(ventas,
                linkTo(methodOn(VentaController.class).listarVentas()).withSelfRel());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Obtener venta por ID",
        description = "Retorna una venta específica por su ID, con enlaces HATEOAS. Requiere rol ADMIN o EMPLEADO."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Venta encontrada",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Venta.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN o EMPLEADO", content = @Content),
        @ApiResponse(responseCode = "404", description = "No existe una venta con el ID indicado", content = @Content)
    })
    public EntityModel<Venta> obtenerVentaPorId(
            @Parameter(description = "ID de la venta a buscar", example = "1", required = true)
            @PathVariable Long id) {
        Venta venta = ventaService.findById(id);
        if (venta == null) {
            return null;
        }
        return toModel(venta);
    }

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Listar ventas por usuario",
        description = "Retorna todas las ventas asociadas a un usuario específico, con enlaces HATEOAS. Requiere rol ADMIN o EMPLEADO."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = Venta.class)))),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN o EMPLEADO", content = @Content)
    })
    public CollectionModel<EntityModel<Venta>> listarVentasPorUsuario(
            @Parameter(description = "ID del usuario", example = "1", required = true)
            @PathVariable Long usuarioId) {
        List<EntityModel<Venta>> ventas = ventaService.findByUsuarioId(usuarioId).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(ventas,
                linkTo(methodOn(VentaController.class).listarVentasPorUsuario(usuarioId)).withSelfRel(),
                linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(usuarioId)).withRel("usuario"));
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
    public EntityModel<Venta> guardarVenta(@RequestBody Venta venta) {
        return toModel(ventaService.save(venta));
    }
}