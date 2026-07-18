package com.minimarket.controller;

import com.jayway.jsonpath.JsonPath;
import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("CarritoControllerTest — CRUD, control de acceso y enlaces HATEOAS")
class CarritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Long productoId;
    private Long usuarioId;

    @BeforeEach
    void setUp() {
        Categoria categoria = categoriaRepository.findAll().stream()
                .filter(c -> "TestCarritoCategoria".equals(c.getNombre()))
                .findFirst()
                .orElseGet(() -> {
                    Categoria c = new Categoria();
                    c.setNombre("TestCarritoCategoria");
                    return categoriaRepository.save(c);
                });

        Producto producto = productoRepository.findAll().stream()
                .filter(p -> "TestCarritoProducto".equals(p.getNombre()))
                .findFirst()
                .orElseGet(() -> {
                    Producto p = new Producto();
                    p.setNombre("TestCarritoProducto");
                    p.setPrecio(1000.0);
                    p.setStock(50);
                    p.setCategoria(categoria);
                    return productoRepository.save(p);
                });
        productoId = producto.getId();

        Usuario usuario = usuarioRepository.findAll().stream()
                .filter(u -> "test_carrito_user".equals(u.getUsername()))
                .findFirst()
                .orElseGet(() -> {
                    Usuario u = new Usuario();
                    u.setUsername("test_carrito_user");
                    u.setPassword("$2a$10$hasheado");
                    return usuarioRepository.save(u);
                });
        usuarioId = usuario.getId();
    }

    private String carritoJson(int cantidad) {
        return String.format(
                "{\"usuario\":{\"id\":%d},\"producto\":{\"id\":%d},\"cantidad\":%d}",
                usuarioId, productoId, cantidad);
    }

    // Control de acceso

    @Test
    @DisplayName("GET /api/carrito sin token retorna 403")
    void listarCarrito_sinToken_retorna403() throws Exception {
        mockMvc.perform(get("/api/carrito"))
                .andExpect(status().isForbidden());
    }

    // Control de acceso: EMPLEADO no puede acceder a la lista de carrito
    @Test
    @WithMockUser(roles = "EMPLEADO")
    @DisplayName("GET /api/carrito con EMPLEADO retorna 403 (rol no permitido)")
    void listarCarrito_comoEmpleado_retorna403() throws Exception {
        mockMvc.perform(get("/api/carrito"))
                .andExpect(status().isForbidden());
    }

    // Control de acceso: CLIENTE puede acceder a la lista de carrito
    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("GET /api/carrito con CLIENTE retorna 200")
    void listarCarrito_comoCliente_retorna200() throws Exception {
        mockMvc.perform(get("/api/carrito"))
                .andExpect(status().isOk());
    }

    // Ciclo completo CRUD + HATEOAS

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("POST /api/carrito agrega un item y retorna los enlaces HATEOAS correctos")
    void agregarProducto_comoCliente_retornaCarritoConLinks() throws Exception {
        mockMvc.perform(post("/api/carrito")
                .contentType(MediaType.APPLICATION_JSON)
                .content(carritoJson(3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(3))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.usuario.href").exists())
                .andExpect(jsonPath("$._links.producto.href").exists());
    }

    // Ciclo completo CRUD: crear, obtener, actualizar y eliminar un item de carrito
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Ciclo completo: crear, obtener, actualizar y eliminar un item de carrito")
    void carrito_cicloCompletoCRUD() throws Exception {
        String creado = mockMvc.perform(post("/api/carrito")
                .contentType(MediaType.APPLICATION_JSON)
                .content(carritoJson(2)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long id = Long.valueOf(JsonPath.read(creado, "$.id").toString());

        mockMvc.perform(get("/api/carrito/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(2));

        mockMvc.perform(put("/api/carrito/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(carritoJson(5)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(5));

        mockMvc.perform(delete("/api/carrito/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/carrito/" + id))
                .andExpect(status().isNotFound());
    }

    // Validación de errores y casos límite
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/carrito/{id} con id inexistente retorna 404")
    void obtenerCarrito_idInexistente_retorna404() throws Exception {
        mockMvc.perform(get("/api/carrito/999999"))
                .andExpect(status().isNotFound());
    }

    // Control de errores
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/carrito/{id} con id inexistente retorna 404")
    void actualizarCarrito_idInexistente_retorna404() throws Exception {
        mockMvc.perform(put("/api/carrito/999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(carritoJson(1)))
                .andExpect(status().isNotFound());
    }

    // HATEOAS: Verificar que los enlaces generados sean correctos
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/carrito/usuario/{usuarioId} incluye el link al usuario correcto")
    void listarPorUsuario_incluyeLinkAlUsuarioCorrecto() throws Exception {
        mockMvc.perform(post("/api/carrito")
                .contentType(MediaType.APPLICATION_JSON)
                .content(carritoJson(1)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/carrito/usuario/" + usuarioId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/api/usuarios/" + usuarioId)));
    }
}