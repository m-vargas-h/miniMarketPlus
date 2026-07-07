package com.minimarket.controller;

import com.minimarket.entity.Categoria;
import com.minimarket.service.CategoriaService;
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
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = Categoria.class))))
    })
    public List<Categoria> listarCategorias() {
        return categoriaService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener categoría por ID",
        description = "Retorna una categoría específica por su ID. Acceso público."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categoría encontrada",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Categoria.class))),
        @ApiResponse(responseCode = "404", description = "No existe una categoría con el ID indicado", content = @Content)
    })
    public ResponseEntity<Categoria> obtenerCategoriaPorId(
            @Parameter(description = "ID de la categoría a buscar", example = "1", required = true)
            @PathVariable Long id) {
        Categoria categoria = categoriaService.findById(id);
        return (categoria != null) ? ResponseEntity.ok(categoria) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Crear categoría",
        description = "Registra una nueva categoría en el sistema. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categoría creada correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Categoria.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN", content = @Content)
    })
    public Categoria guardarCategoria(@RequestBody Categoria categoria) {
        return categoriaService.save(categoria);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Actualizar categoría",
        description = "Modifica los datos de una categoría existente. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categoría actualizada correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Categoria.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN", content = @Content),
        @ApiResponse(responseCode = "404", description = "No existe una categoría con el ID indicado", content = @Content)
    })
    public ResponseEntity<Categoria> actualizarCategoria(
            @Parameter(description = "ID de la categoría a actualizar", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody Categoria categoria) {
        Categoria categoriaExistente = categoriaService.findById(id);
        if (categoriaExistente != null) {
            categoria.setId(id);
            return ResponseEntity.ok(categoriaService.save(categoria));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Eliminar categoría",
        description = "Elimina una categoría por su ID. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Categoría eliminada correctamente", content = @Content),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN", content = @Content),
        @ApiResponse(responseCode = "404", description = "No existe una categoría con el ID indicado", content = @Content)
    })
    public ResponseEntity<Void> eliminarCategoria(
            @Parameter(description = "ID de la categoría a eliminar", example = "1", required = true)
            @PathVariable Long id) {
        Categoria categoria = categoriaService.findById(id);
        if (categoria != null) {
            categoriaService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}