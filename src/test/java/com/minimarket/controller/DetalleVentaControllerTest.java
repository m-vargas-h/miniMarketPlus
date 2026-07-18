package com.minimarket.controller;

import com.jayway.jsonpath.JsonPath;
import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.repository.VentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.Locale;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("DetalleVentaControllerTest — CRUD, control de acceso y enlaces HATEOAS")
class DetalleVentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VentaRepository ventaRepository;

    private Long productoId;
    private Long ventaId;

    @BeforeEach
    void setUp() {
        Categoria categoria = categoriaRepository.findAll().stream()
                .filter(c -> "TestDetalleCategoria".equals(c.getNombre()))
                .findFirst()
                .orElseGet(() -> {
                    Categoria c = new Categoria();
                    c.setNombre("TestDetalleCategoria");
                    return categoriaRepository.save(c);
                });

        Producto producto = productoRepository.findAll().stream()
                .filter(p -> "TestDetalleProducto".equals(p.getNombre()))
                .findFirst()
                .orElseGet(() -> {
                    Producto p = new Producto();
                    p.setNombre("TestDetalleProducto");
                    p.setPrecio(2000.0);
                    p.setStock(30);
                    p.setCategoria(categoria);
                    return productoRepository.save(p);
                });
        productoId = producto.getId();

        Usuario usuario = usuarioRepository.findAll().stream()
                .filter(u -> "test_detalle_user".equals(u.getUsername()))
                .findFirst()
                .orElseGet(() -> {
                    Usuario u = new Usuario();
                    u.setUsername("test_detalle_user");
                    u.setPassword("$2a$10$hasheado");
                    return usuarioRepository.save(u);
                });

        Venta venta = new Venta();
        venta.setUsuario(usuario);
        venta.setFecha(new Date());
        ventaId = ventaRepository.save(venta).getId();
    }

    private String detalleJson(int cantidad, double precio) {
        return String.format(Locale.US,
                "{\"venta\":{\"id\":%d},\"producto\":{\"id\":%d},\"cantidad\":%d,\"precio\":%.2f}",
                ventaId, productoId, cantidad, precio);
    }

    // Control de acceso

    // Control de acceso: sin token retorna 403
    @Test
    @DisplayName("GET /api/detalle-ventas sin token retorna 403")
    void listar_sinToken_retorna403() throws Exception {
        mockMvc.perform(get("/api/detalle-ventas"))
                .andExpect(status().isForbidden());
    }

    // Control de acceso: CLIENTE no puede listar ni crear detalles de venta
    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("GET /api/detalle-ventas con CLIENTE retorna 403 (rol no permitido)")
    void listar_comoCliente_retorna403() throws Exception {
        mockMvc.perform(get("/api/detalle-ventas"))
                .andExpect(status().isForbidden());
    }

    // Control de acceso: EMPLEADO puede listar, ADMIN puede listar y eliminar
    @Test
    @WithMockUser(roles = "EMPLEADO")
    @DisplayName("GET /api/detalle-ventas con EMPLEADO retorna 200")
    void listar_comoEmpleado_retorna200() throws Exception {
        mockMvc.perform(get("/api/detalle-ventas"))
                .andExpect(status().isOk());
    }

    // Ciclo CRUD + HATEOAS
    @Test
    @WithMockUser(roles = "EMPLEADO")
    @DisplayName("POST /api/detalle-ventas crea un detalle y retorna los enlaces HATEOAS correctos")
    void crear_comoEmpleado_retornaDetalleConLinks() throws Exception {
        mockMvc.perform(post("/api/detalle-ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(detalleJson(4, 2000.0)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(4))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.venta.href").exists())
                .andExpect(jsonPath("$._links.producto.href").exists());
    }

    // Ciclo completo CRUD + HATEOAS
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Ciclo completo: crear, obtener, actualizar y eliminar un detalle de venta")
    void detalleVenta_cicloCompletoCRUD() throws Exception {
        String creado = mockMvc.perform(post("/api/detalle-ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(detalleJson(1, 2000.0)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long id = Long.valueOf(JsonPath.read(creado, "$.id").toString());

        mockMvc.perform(get("/api/detalle-ventas/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(1));

        mockMvc.perform(put("/api/detalle-ventas/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(detalleJson(7, 2000.0)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(7));

        mockMvc.perform(delete("/api/detalle-ventas/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/detalle-ventas/" + id))
                .andExpect(status().isNotFound());
    }

    // Casos especiales
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/detalle-ventas/{id} con id inexistente retorna 404")
    void obtener_idInexistente_retorna404() throws Exception {
        mockMvc.perform(get("/api/detalle-ventas/999999"))
                .andExpect(status().isNotFound());
    }

    // Caso especial: DELETE solo permitido para ADMIN, no para EMPLEADO
    @Test
    @WithMockUser(roles = "EMPLEADO")
    @DisplayName("DELETE /api/detalle-ventas/{id} con EMPLEADO retorna 403 (solo ADMIN puede eliminar)")
    void eliminar_comoEmpleado_retorna403() throws Exception {
        mockMvc.perform(delete("/api/detalle-ventas/1"))
                .andExpect(status().isForbidden());
    }

    // Caso especial: DELETE con id inexistente retorna 404, incluso para ADMIN
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/detalle-ventas/{id} con id inexistente retorna 404")
    void eliminar_comoAdmin_idInexistente_retorna404() throws Exception {
        mockMvc.perform(delete("/api/detalle-ventas/999999"))
                .andExpect(status().isNotFound());
    }

    // HATEOAS: verificar que los enlaces a la venta sean correctos
    @Test
    @WithMockUser(roles = "EMPLEADO")
    @DisplayName("GET /api/detalle-ventas/venta/{ventaId} incluye el link a la venta correcta")
    void listarPorVenta_incluyeLinkALaVentaCorrecta() throws Exception {
        mockMvc.perform(post("/api/detalle-ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(detalleJson(2, 2000.0)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/detalle-ventas/venta/" + ventaId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/api/ventas/" + ventaId)));
    }
}