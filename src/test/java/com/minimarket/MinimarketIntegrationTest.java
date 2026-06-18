package com.minimarket;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.service.UsuarioService;
import com.minimarket.service.VentaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

@SpringBootTest
@AutoConfigureMockMvc
class MinimarketIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private VentaService ventaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    // ─── Contexto de Spring ───

    @Test
    void testContexto_CargaCorrectamente() {
        assertNotNull(mockMvc);
        assertNotNull(usuarioService);
        assertNotNull(ventaService);
    }

    // ─── Datos iniciales (data.sql) ───

    @Test
    void testDataSql_UsuariosSeCarganCorrectamente() {
        Optional<Usuario> admin = usuarioService.findByUsername("admin");
        Optional<Usuario> empleado = usuarioService.findByUsername("empleado");
        Optional<Usuario> cliente = usuarioService.findByUsername("cliente");

        assertTrue(admin.isPresent());
        assertTrue(empleado.isPresent());
        assertTrue(cliente.isPresent());
    }

    @Test
    void testDataSql_RolesSeCarganCorrectamente() {
        List<Rol> roles = rolRepository.findAll();
        assertFalse(roles.isEmpty());
        assertTrue(roles.stream().anyMatch(r -> r.getNombre().equals("ROLE_ADMIN")));
        assertTrue(roles.stream().anyMatch(r -> r.getNombre().equals("ROLE_EMPLEADO")));
        assertTrue(roles.stream().anyMatch(r -> r.getNombre().equals("ROLE_CLIENTE")));
    }

    @Test
    void testDataSql_AdminTieneRolCorrecto() {
        Optional<Usuario> admin = usuarioService.findByUsername("admin");
        assertTrue(admin.isPresent());
        boolean tieneRolAdmin = admin.get().getRoles().stream()
                .anyMatch(r -> r.getNombre().equals("ROLE_ADMIN"));
        assertTrue(tieneRolAdmin);
    }

    // ─── Endpoint de login ───

    @Test
    void testLogin_CredencialesValidas_Retorna200() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void testLogin_CredencialesInvalidas_RetornaAccesoDenegado() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"wrongpass\"}"))
                .andExpect(status().isForbidden());
    }

    // ─── Endpoints protegidos sin token ───

    @Test
    void testEndpointVentas_SinToken_Retorna403() throws Exception {
        mockMvc.perform(get("/api/ventas"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testEndpointUsuarios_SinToken_Retorna403() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isForbidden());
    }

    // ─── Endpoints públicos ───

    @Test
    void testEndpointProductos_SinToken_Retorna200() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk());
    }

    @Test
    void testEndpointCategorias_SinToken_Retorna200() throws Exception {
        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk());
    }
}