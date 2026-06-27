package com.minimarket.controller;

import com.minimarket.entity.Producto;
import com.minimarket.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Gestión del catálogo de productos — lectura pública, modificación requiere rol ADMIN")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    @Operation(
        summary = "Listar todos los productos",
        description = "Retorna el catálogo completo de productos disponibles en el minimarket. Acceso público."
    )
    public List<Producto> listarProductos() {
        return productoService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener producto por ID",
        description = "Retorna un producto específico por su ID. Acceso público. Retorna 404 si no existe."
    )
    public ResponseEntity<Producto> obtenerProductoPorId(@PathVariable Long id) {
        Producto producto = productoService.findById(id);
        return (producto != null) ? ResponseEntity.ok(producto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Crear producto",
        description = "Agrega un nuevo producto al catálogo. Requiere rol ADMIN."
    )
    public Producto guardarProducto(@RequestBody Producto producto) {
        return productoService.save(producto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Actualizar producto",
        description = "Modifica los datos de un producto existente. Requiere rol ADMIN. Retorna 404 si no existe."
    )
    public ResponseEntity<Producto> actualizarProducto(@PathVariable Long id, @RequestBody Producto producto) {
        Producto productoExistente = productoService.findById(id);
        if (productoExistente != null) {
            producto.setId(id);
            return ResponseEntity.ok(productoService.save(producto));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Eliminar producto",
        description = "Elimina un producto del catálogo por su ID. Requiere rol ADMIN. Retorna 204 si se eliminó correctamente, 404 si no existe."
    )
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        Producto producto = productoService.findById(id);
        if (producto != null) {
            productoService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}