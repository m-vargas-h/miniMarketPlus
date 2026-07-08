package com.minimarket.controller;

import com.minimarket.entity.Producto;
import com.minimarket.service.ProductoService;
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
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Gestión del catálogo de productos — lectura pública, modificación requiere rol ADMIN")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    // --- Helper: arma un EntityModel<Producto> con sus enlaces HATEOAS ---
    private EntityModel<Producto> toModel(Producto producto) {
        EntityModel<Producto> model = EntityModel.of(producto);
        model.add(linkTo(methodOn(ProductoController.class).obtenerProductoPorId(producto.getId())).withSelfRel());
        model.add(linkTo(methodOn(ProductoController.class).listarProductos()).withRel("productos"));
        if (producto.getCategoria() != null) {
            model.add(linkTo(methodOn(CategoriaController.class).obtenerCategoriaPorId(producto.getCategoria().getId()))
                    .withRel("categoria"));
        }
        return model;
    }

    @GetMapping
    @Operation(
        summary = "Listar todos los productos",
        description = "Retorna el catálogo completo de productos con enlaces HATEOAS. Acceso público."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = Producto.class))))
    })
    public CollectionModel<EntityModel<Producto>> listarProductos() {
        List<EntityModel<Producto>> productos = productoService.findAll().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(productos,
                linkTo(methodOn(ProductoController.class).listarProductos()).withSelfRel());
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener producto por ID",
        description = "Retorna un producto específico por su ID con enlaces HATEOAS. Acceso público."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto encontrado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Producto.class))),
        @ApiResponse(responseCode = "404", description = "No existe un producto con el ID indicado", content = @Content)
    })
    public EntityModel<Producto> obtenerProductoPorId(
            @Parameter(description = "ID del producto a buscar", example = "1", required = true)
            @PathVariable Long id) {
        Producto producto = productoService.findById(id);
        if (producto == null) {
            return null;
        }
        return toModel(producto);
    }

    @GetMapping("/categoria/{categoriaId}")
    @Operation(
        summary = "Listar productos por categoría",
        description = "Retorna todos los productos que pertenecen a una categoría específica, con enlaces HATEOAS. Acceso público."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = Producto.class))))
    })
    public CollectionModel<EntityModel<Producto>> listarProductosPorCategoria(
            @Parameter(description = "ID de la categoría", example = "1", required = true)
            @PathVariable Long categoriaId) {
        List<EntityModel<Producto>> productos = productoService.findByCategoriaId(categoriaId).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(productos,
                linkTo(methodOn(ProductoController.class).listarProductosPorCategoria(categoriaId)).withSelfRel(),
                linkTo(methodOn(CategoriaController.class).obtenerCategoriaPorId(categoriaId)).withRel("categoria"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Crear producto",
        description = "Registra un nuevo producto en el catálogo. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto creado correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Producto.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN", content = @Content)
    })
    public EntityModel<Producto> guardarProducto(@RequestBody Producto producto) {
        return toModel(productoService.save(producto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Actualizar producto",
        description = "Modifica los datos de un producto existente. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto actualizado correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Producto.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN", content = @Content),
        @ApiResponse(responseCode = "404", description = "No existe un producto con el ID indicado", content = @Content)
    })
    public ResponseEntity<EntityModel<Producto>> actualizarProducto(
            @Parameter(description = "ID del producto a actualizar", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody Producto producto) {
        Producto existente = productoService.findById(id);
        if (existente != null) {
            producto.setId(id);
            return ResponseEntity.ok(toModel(productoService.save(producto)));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Eliminar producto",
        description = "Elimina un producto del catálogo por su ID. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Producto eliminado correctamente", content = @Content),
        @ApiResponse(responseCode = "401", description = "No autenticado — falta el token JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN", content = @Content),
        @ApiResponse(responseCode = "404", description = "No existe un producto con el ID indicado", content = @Content)
    })
    public ResponseEntity<Void> eliminarProducto(
            @Parameter(description = "ID del producto a eliminar", example = "1", required = true)
            @PathVariable Long id) {
        Producto producto = productoService.findById(id);
        if (producto != null) {
            productoService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}