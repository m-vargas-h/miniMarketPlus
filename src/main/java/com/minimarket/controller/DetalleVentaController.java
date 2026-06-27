package com.minimarket.controller;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.service.DetalleVentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Operation(
        summary = "Listar todos los detalles de venta",
        description = "Retorna la lista completa de líneas de detalle asociadas a ventas. Requiere rol ADMIN o EMPLEADO."
    )
    public List<DetalleVenta> listarDetalleVentas() {
        return detalleVentaService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @Operation(
        summary = "Obtener detalle de venta por ID",
        description = "Retorna una línea de detalle específica por su ID. Requiere rol ADMIN o EMPLEADO. Retorna 404 si no existe."
    )
    public ResponseEntity<DetalleVenta> obtenerDetalleVentaPorId(@PathVariable Long id) {
        DetalleVenta detalleVenta = detalleVentaService.findById(id);
        return (detalleVenta != null) ? ResponseEntity.ok(detalleVenta) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @Operation(
        summary = "Registrar detalle de venta",
        description = "Crea una nueva línea de detalle asociada a una venta existente. Requiere rol ADMIN o EMPLEADO."
    )
    public DetalleVenta guardarDetalleVenta(@RequestBody DetalleVenta detalleVenta) {
        return detalleVentaService.save(detalleVenta);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @Operation(
        summary = "Actualizar detalle de venta",
        description = "Modifica los datos de una línea de detalle existente. Requiere rol ADMIN o EMPLEADO. Retorna 404 si no existe."
    )
    public ResponseEntity<DetalleVenta> actualizarDetalleVenta(@PathVariable Long id, @RequestBody DetalleVenta detalleVenta) {
        DetalleVenta existente = detalleVentaService.findById(id);
        if (existente != null) {
            detalleVenta.setId(id);
            return ResponseEntity.ok(detalleVentaService.save(detalleVenta));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Eliminar detalle de venta",
        description = "Elimina una línea de detalle por su ID. Requiere rol ADMIN. Retorna 204 si se eliminó correctamente, 404 si no existe."
    )
    public ResponseEntity<Void> eliminarDetalleVenta(@PathVariable Long id) {
        DetalleVenta detalleVenta = detalleVentaService.findById(id);
        if (detalleVenta != null) {
            detalleVentaService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}