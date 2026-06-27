package com.minimarket.controller;

import com.minimarket.entity.Venta;
import com.minimarket.service.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Operation(
        summary = "Listar todas las ventas",
        description = "Retorna el historial completo de ventas registradas. Requiere rol ADMIN o EMPLEADO."
    )
    public List<Venta> listarVentas() {
        return ventaService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @Operation(
        summary = "Obtener venta por ID",
        description = "Retorna una venta específica por su ID. Requiere rol ADMIN o EMPLEADO. Retorna 404 si no existe."
    )
    public ResponseEntity<Venta> obtenerVentaPorId(@PathVariable Long id) {
        Venta venta = ventaService.findById(id);
        return (venta != null) ? ResponseEntity.ok(venta) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @Operation(
        summary = "Registrar venta",
        description = "Crea un nuevo registro de venta en el sistema. Requiere rol ADMIN o EMPLEADO."
    )
    public Venta guardarVenta(@RequestBody Venta venta) {
        return ventaService.save(venta);
    }
}