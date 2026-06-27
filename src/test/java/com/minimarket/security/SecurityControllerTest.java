package com.minimarket.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
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
        // Categoría base requerida por Producto
        if (categoriaRepository.findById(1L).isEmpty()) {
            com.minimarket.entity.Categoria cat = new com.minimarket.entity.Categoria();
            cat.setNombre("Lácteos");
            categoriaRepository.save(cat);
        }

        // Producto base requerido por Inventario
        if (productoRepository.findById(1L).isEmpty()) {
            com.minimarket.entity.Categoria cat = categoriaRepository.findById(1L).orElseThrow();
            com.minimarket.entity.Producto prod = new com.minimarket.entity.Producto();
            prod.setNombre("Leche");
            prod.setPrecio(990.0);
            prod.setStock(50);
            prod.setCategoria(cat);
            productoRepository.save(prod);
        }

        // Usuario base requerido por Venta
        if (usuarioRepository.findById(1L).isEmpty()) {
            com.minimarket.entity.Usuario user = new com.minimarket.entity.Usuario();
            user.setUsername("test_user");
            user.setPassword("$2a$10$hasheado");
            usuarioRepository.save(user);
        }
    }

    // Producto: endpoints públicos (GET)

    @Test
    void testProducto_ListarSinToken_Retorna200() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk());
    }

    @Test
    void testProducto_ObtenerPorIdSinToken_Retorna200oNotFound() throws Exception {
        mockMvc.perform(get("/api/productos/1"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 200 || status == 404,
                            "Se esperaba 200 o 404, pero fue: " + status);
                });
    }

    // Producto: POST/PUT/DELETE solo ADMIN

    @Test
    void testProducto_CrearSinToken_Retorna403() throws Exception {
        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Leche\",\"precio\":990.0,\"stock\":20}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void testProducto_CrearComoCliente_Retorna403() throws Exception {
        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Leche\",\"precio\":990.0,\"stock\":20}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void testProducto_CrearComoEmpleado_Retorna403() throws Exception {
        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Leche\",\"precio\":990.0,\"stock\":20}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testProducto_CrearComoAdmin_Retorna200() throws Exception {
        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Leche\",\"precio\":990.0,\"stock\":20," +
                        "\"categoria\":{\"id\":1}}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testProducto_EliminarComoAdmin_Retorna204oNotFound() throws Exception {
        mockMvc.perform(delete("/api/productos/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void testProducto_EliminarComoEmpleado_Retorna403() throws Exception {
        mockMvc.perform(delete("/api/productos/1"))
                .andExpect(status().isForbidden());
    }

    // Inventario: solo ADMIN y EMPLEADO

    @Test
    void testInventario_ListarSinToken_Retorna403() throws Exception {
        mockMvc.perform(get("/api/inventario"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void testInventario_ListarComoCliente_Retorna403() throws Exception {
        mockMvc.perform(get("/api/inventario"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void testInventario_ListarComoEmpleado_Retorna200() throws Exception {
        mockMvc.perform(get("/api/inventario"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testInventario_ListarComoAdmin_Retorna200() throws Exception {
        mockMvc.perform(get("/api/inventario"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void testInventario_RegistrarMovimientoComoEmpleado_Retorna200() throws Exception {
        mockMvc.perform(post("/api/inventario")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cantidad\":10,\"tipoMovimiento\":\"Entrada\"," +
                        "\"fechaMovimiento\":\"2026-06-26T00:00:00.000+00:00\"," +
                        "\"producto\":{\"id\":1}}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void testInventario_RegistrarMovimientoComoCliente_Retorna403() throws Exception {
        mockMvc.perform(post("/api/inventario")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cantidad\":10,\"tipoMovimiento\":\"Entrada\",\"producto\":{\"id\":1}}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void testInventario_EliminarComoEmpleado_Retorna403() throws Exception {
        mockMvc.perform(delete("/api/inventario/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testInventario_EliminarComoAdmin_Retorna204oNotFound() throws Exception {
        mockMvc.perform(delete("/api/inventario/999"))
                .andExpect(status().isNotFound());
    }

    // Venta: solo ADMIN y EMPLEADO

    @Test
    void testVenta_ListarSinToken_Retorna403() throws Exception {
        mockMvc.perform(get("/api/ventas"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void testVenta_ListarComoCliente_Retorna403() throws Exception {
        mockMvc.perform(get("/api/ventas"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void testVenta_ListarComoEmpleado_Retorna200() throws Exception {
        mockMvc.perform(get("/api/ventas"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testVenta_ListarComoAdmin_Retorna200() throws Exception {
        mockMvc.perform(get("/api/ventas"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void testVenta_CrearComoCliente_Retorna403() throws Exception {
        mockMvc.perform(post("/api/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"usuario\":{\"id\":1},\"detalles\":[]}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void testVenta_CrearComoEmpleado_Retorna200() throws Exception {
        mockMvc.perform(post("/api/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fecha\":\"2026-06-26T00:00:00.000+00:00\"," +
                        "\"usuario\":{\"id\":1},\"detalles\":[]}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testVenta_CrearComoAdmin_Retorna200() throws Exception {
        mockMvc.perform(post("/api/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fecha\":\"2026-06-26T00:00:00.000+00:00\"," +
                        "\"usuario\":{\"id\":1},\"detalles\":[]}"))
                .andExpect(status().isOk());
    }

    // Usuario: solo ADMIN

    @Test
    void testUsuario_ListarSinToken_Retorna403() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void testUsuario_ListarComoEmpleado_Retorna403() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void testUsuario_ListarComoCliente_Retorna403() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUsuario_ListarComoAdmin_Retorna200() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUsuario_ObtenerPorIdComoAdmin_Retorna200oNotFound() throws Exception {
        mockMvc.perform(get("/api/usuarios/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void testUsuario_ObtenerPorIdComoEmpleado_Retorna403() throws Exception {
        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUsuario_EliminarComoAdmin_Retorna204oNotFound() throws Exception {
        mockMvc.perform(delete("/api/usuarios/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void testUsuario_EliminarComoEmpleado_Retorna403() throws Exception {
        mockMvc.perform(delete("/api/usuarios/1"))
                .andExpect(status().isForbidden());
    }
}