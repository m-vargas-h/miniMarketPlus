package com.minimarket.controller;

import com.minimarket.entity.Categoria;
import com.minimarket.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorías", description = "Gestión de categorías de productos — lectura pública, modificación requiere rol ADMIN")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping
    @Operation(
        summary = "Listar todas las categorías",
        description = "Retorna la lista completa de categorías disponibles. Acceso público."
    )
    public List<Categoria> listarCategorias() {
        return categoriaService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener categoría por ID",
        description = "Retorna una categoría específica por su ID. Acceso público. Retorna 404 si no existe."
    )
    public ResponseEntity<Categoria> obtenerCategoriaPorId(@PathVariable Long id) {
        Categoria categoria = categoriaService.findById(id);
        return (categoria != null) ? ResponseEntity.ok(categoria) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Crear categoría",
        description = "Registra una nueva categoría en el sistema. Requiere rol ADMIN."
    )
    public Categoria guardarCategoria(@RequestBody Categoria categoria) {
        return categoriaService.save(categoria);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Actualizar categoría",
        description = "Modifica los datos de una categoría existente. Requiere rol ADMIN. Retorna 404 si no existe."
    )
    public ResponseEntity<Categoria> actualizarCategoria(@PathVariable Long id, @RequestBody Categoria categoria) {
        Categoria categoriaExistente = categoriaService.findById(id);
        if (categoriaExistente != null) {
            categoria.setId(id);
            return ResponseEntity.ok(categoriaService.save(categoria));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Eliminar categoría",
        description = "Elimina una categoría por su ID. Requiere rol ADMIN. Retorna 204 si se eliminó correctamente, 404 si no existe."
    )
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id) {
        Categoria categoria = categoriaService.findById(id);
        if (categoria != null) {
            categoriaService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}