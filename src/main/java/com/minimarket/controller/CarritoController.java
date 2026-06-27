package com.minimarket.controller;

import com.minimarket.entity.Carrito;
import com.minimarket.service.CarritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Operation(
        summary = "Listar items del carrito",
        description = "Retorna todos los productos agregados al carrito."
    )
    public List<Carrito> listarCarrito() {
        return carritoService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    @Operation(
        summary = "Obtener item del carrito por ID",
        description = "Retorna un item específico del carrito. Retorna 404 si no existe."
    )
    public ResponseEntity<Carrito> obtenerCarritoPorId(@PathVariable Long id) {
        Carrito carrito = carritoService.findById(id);
        return (carrito != null) ? ResponseEntity.ok(carrito) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    @Operation(
        summary = "Agregar producto al carrito",
        description = "Agrega un nuevo producto al carrito de compras."
    )
    public Carrito agregarProductoAlCarrito(@RequestBody Carrito carrito) {
        return carritoService.save(carrito);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    @Operation(
        summary = "Actualizar item del carrito",
        description = "Modifica la cantidad u otros datos de un producto en el carrito. Retorna 404 si no existe."
    )
    public ResponseEntity<Carrito> actualizarCarrito(@PathVariable Long id, @RequestBody Carrito carrito) {
        Carrito existente = carritoService.findById(id);
        if (existente != null) {
            carrito.setId(id);
            return ResponseEntity.ok(carritoService.save(carrito));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    @Operation(
        summary = "Eliminar producto del carrito",
        description = "Elimina un producto del carrito por su ID. Retorna 204 si se eliminó correctamente, 404 si no existe."
    )
    public ResponseEntity<Void> eliminarProductoDelCarrito(@PathVariable Long id) {
        Carrito carrito = carritoService.findById(id);
        if (carrito != null) {
            carritoService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}