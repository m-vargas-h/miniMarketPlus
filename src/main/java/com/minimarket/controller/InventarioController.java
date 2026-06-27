package com.minimarket.controller;

import com.minimarket.entity.Inventario;
import com.minimarket.service.InventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
@Tag(name = "Inventario", description = "Gestión de movimientos de inventario — requiere rol ADMIN o EMPLEADO. Eliminación solo ADMIN.")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @Operation(
        summary = "Listar movimientos de inventario",
        description = "Retorna todos los movimientos de entrada y salida registrados en el inventario. Requiere rol ADMIN o EMPLEADO."
    )
    public List<Inventario> listarMovimientosDeInventario() {
        return inventarioService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @Operation(
        summary = "Obtener movimiento por ID",
        description = "Retorna un movimiento de inventario específico por su ID. Requiere rol ADMIN o EMPLEADO. Retorna 404 si no existe."
    )
    public ResponseEntity<Inventario> obtenerMovimientoPorId(@PathVariable Long id) {
        Inventario inventario = inventarioService.findById(id);
        return (inventario != null) ? ResponseEntity.ok(inventario) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @Operation(
        summary = "Registrar movimiento de inventario",
        description = "Registra un nuevo movimiento de entrada o salida de stock. Requiere rol ADMIN o EMPLEADO."
    )
    public Inventario registrarMovimiento(@RequestBody Inventario inventario) {
        return inventarioService.save(inventario);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @Operation(
        summary = "Actualizar movimiento de inventario",
        description = "Modifica los datos de un movimiento de inventario existente. Requiere rol ADMIN o EMPLEADO. Retorna 404 si no existe."
    )
    public ResponseEntity<Inventario> actualizarMovimiento(@PathVariable Long id, @RequestBody Inventario inventario) {
        Inventario existente = inventarioService.findById(id);
        if (existente != null) {
            inventario.setId(id);
            return ResponseEntity.ok(inventarioService.save(inventario));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Eliminar movimiento de inventario",
        description = "Elimina un movimiento de inventario por su ID. Requiere rol ADMIN. Retorna 204 si se eliminó correctamente, 404 si no existe."
    )
    public ResponseEntity<Void> eliminarMovimiento(@PathVariable Long id) {
        Inventario inventario = inventarioService.findById(id);
        if (inventario != null) {
            inventarioService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}