package com.minimarket.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("SecurityControllerTest — Validación de control de acceso por rol en controllers REST")
class SecurityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.minimarket.repository.CategoriaRepository categoriaRepository;

    @Autowired
    private com.minimarket.repository.ProductoRepository productoRepository;

    @Autowired
    private com.minimarket.repository.UsuarioRepository usuarioRepository;


    @BeforeEach
    void setUp() {
        if (categoriaRepository.findById(1L).isEmpty()) {
            com.minimarket.entity.Categoria cat = new com.minimarket.entity.Categoria();
            cat.setNombre("Lácteos");
            categoriaRepository.save(cat);
        }
        if (productoRepository.findById(1L).isEmpty()) {
            com.minimarket.entity.Categoria cat = categoriaRepository.findById(1L).orElseThrow();
            com.minimarket.entity.Producto prod = new com.minimarket.entity.Producto();
            prod.setNombre("Leche");
            prod.setPrecio(990.0);
            prod.setStock(50);
            prod.setCategoria(cat);
            productoRepository.save(prod);
        }
        if (usuarioRepository.findById(1L).isEmpty()) {
            com.minimarket.entity.Usuario user = new com.minimarket.entity.Usuario();
            user.setUsername("test_user");
            user.setPassword("$2a$10$hasheado");
            usuarioRepository.save(user);
        }
    }

    // Producto — endpoints públicos (GET)

    @Test
    @DisplayName("GET /api/productos es público — retorna 200 sin token")
    void producto_listarSinToken_retorna200() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/productos/{id} es público — retorna 200 o 404 sin token")
    void producto_obtenerPorIdSinToken_retorna200oNotFound() throws Exception {
        mockMvc.perform(get("/api/productos/1"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 200 || status == 404,
                            "Se esperaba 200 o 404, pero fue: " + status);
                });
    }

    // Producto — POST/PUT/DELETE solo ADMIN

    @Test
    @DisplayName("POST /api/productos sin token retorna 403")
    void producto_crearSinToken_retorna403() throws Exception {
        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Leche\",\"precio\":990.0,\"stock\":20}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("POST /api/productos con CLIENTE retorna 403")
    void producto_crearComoCliente_retorna403() throws Exception {
        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Leche\",\"precio\":990.0,\"stock\":20}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    @DisplayName("POST /api/productos con EMPLEADO retorna 403")
    void producto_crearComoEmpleado_retorna403() throws Exception {
        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Leche\",\"precio\":990.0,\"stock\":20}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/productos con ADMIN retorna 200")
    void producto_crearComoAdmin_retorna200() throws Exception {
        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Leche\",\"precio\":990.0,\"stock\":20," +
                        "\"categoria\":{\"id\":1}}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/productos/{id} con ADMIN retorna 404 si no existe")
    void producto_eliminarComoAdmin_retornaNotFound() throws Exception {
        mockMvc.perform(delete("/api/productos/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    @DisplayName("DELETE /api/productos/{id} con EMPLEADO retorna 403")
    void producto_eliminarComoEmpleado_retorna403() throws Exception {
        mockMvc.perform(delete("/api/productos/1"))
                .andExpect(status().isForbidden());
    }

    // Inventario — solo ADMIN y EMPLEADO

    @Test
    @DisplayName("GET /api/inventario sin token retorna 403")
    void inventario_listarSinToken_retorna403() throws Exception {
        mockMvc.perform(get("/api/inventario"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("GET /api/inventario con CLIENTE retorna 403")
    void inventario_listarComoCliente_retorna403() throws Exception {
        mockMvc.perform(get("/api/inventario"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    @DisplayName("GET /api/inventario con EMPLEADO retorna 200")
    void inventario_listarComoEmpleado_retorna200() throws Exception {
        mockMvc.perform(get("/api/inventario"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/inventario con ADMIN retorna 200")
    void inventario_listarComoAdmin_retorna200() throws Exception {
        mockMvc.perform(get("/api/inventario"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    @DisplayName("POST /api/inventario con EMPLEADO retorna 200")
    void inventario_registrarMovimientoComoEmpleado_retorna200() throws Exception {
        mockMvc.perform(post("/api/inventario")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cantidad\":10,\"tipoMovimiento\":\"Entrada\"," +
                        "\"fechaMovimiento\":\"2026-06-26T00:00:00.000+00:00\"," +
                        "\"producto\":{\"id\":1}}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("POST /api/inventario con CLIENTE retorna 403")
    void inventario_registrarMovimientoComoCliente_retorna403() throws Exception {
        mockMvc.perform(post("/api/inventario")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cantidad\":10,\"tipoMovimiento\":\"Entrada\",\"producto\":{\"id\":1}}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    @DisplayName("DELETE /api/inventario/{id} con EMPLEADO retorna 403")
    void inventario_eliminarComoEmpleado_retorna403() throws Exception {
        mockMvc.perform(delete("/api/inventario/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/inventario/{id} con ADMIN retorna 404 si no existe")
    void inventario_eliminarComoAdmin_retornaNotFound() throws Exception {
        mockMvc.perform(delete("/api/inventario/999"))
                .andExpect(status().isNotFound());
    }

    // Venta — solo ADMIN y EMPLEADO

    @Test
    @DisplayName("GET /api/ventas sin token retorna 403")
    void venta_listarSinToken_retorna403() throws Exception {
        mockMvc.perform(get("/api/ventas"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("GET /api/ventas con CLIENTE retorna 403")
    void venta_listarComoCliente_retorna403() throws Exception {
        mockMvc.perform(get("/api/ventas"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    @DisplayName("GET /api/ventas con EMPLEADO retorna 200")
    void venta_listarComoEmpleado_retorna200() throws Exception {
        mockMvc.perform(get("/api/ventas"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/ventas con ADMIN retorna 200")
    void venta_listarComoAdmin_retorna200() throws Exception {
        mockMvc.perform(get("/api/ventas"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("POST /api/ventas con CLIENTE retorna 403")
    void venta_crearComoCliente_retorna403() throws Exception {
        mockMvc.perform(post("/api/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"usuario\":{\"id\":1},\"detalles\":[]}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    @DisplayName("POST /api/ventas con EMPLEADO retorna 200")
    void venta_crearComoEmpleado_retorna200() throws Exception {
        mockMvc.perform(post("/api/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fecha\":\"2026-06-26T00:00:00.000+00:00\"," +
                        "\"usuario\":{\"id\":1},\"detalles\":[]}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/ventas con ADMIN retorna 200")
    void venta_crearComoAdmin_retorna200() throws Exception {
        mockMvc.perform(post("/api/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fecha\":\"2026-06-26T00:00:00.000+00:00\"," +
                        "\"usuario\":{\"id\":1},\"detalles\":[]}"))
                .andExpect(status().isOk());
    }

    // Usuario — solo ADMINs

    @Test
    @DisplayName("GET /api/usuarios sin token retorna 403")
    void usuario_listarSinToken_retorna403() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    @DisplayName("GET /api/usuarios con EMPLEADO retorna 403")
    void usuario_listarComoEmpleado_retorna403() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("GET /api/usuarios con CLIENTE retorna 403")
    void usuario_listarComoCliente_retorna403() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/usuarios con ADMIN retorna 200")
    void usuario_listarComoAdmin_retorna200() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/usuarios/{id} con ADMIN retorna 404 si no existe")
    void usuario_obtenerPorIdComoAdmin_retornaNotFound() throws Exception {
        mockMvc.perform(get("/api/usuarios/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    @DisplayName("GET /api/usuarios/{id} con EMPLEADO retorna 403")
    void usuario_obtenerPorIdComoEmpleado_retorna403() throws Exception {
        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/usuarios/{id} con ADMIN retorna 404 si no existe")
    void usuario_eliminarComoAdmin_retornaNotFound() throws Exception {
        mockMvc.perform(delete("/api/usuarios/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    @DisplayName("DELETE /api/usuarios/{id} con EMPLEADO retorna 403")
    void usuario_eliminarComoEmpleado_retorna403() throws Exception {
        mockMvc.perform(delete("/api/usuarios/1"))
                .andExpect(status().isForbidden());
    }
}