package com.minimarket.security.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails userAdmin;
    private UserDetails userEmpleado;
    private UserDetails userCliente;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        userAdmin = new User("admin", "$2a$10$hasheado",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        userEmpleado = new User("empleado", "$2a$10$hasheado",
                List.of(new SimpleGrantedAuthority("ROLE_EMPLEADO")));

        userCliente = new User("cliente", "$2a$10$hasheado",
                List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")));
    }

    // Generación de token

    @Test
    void testGenerateToken_AdminRetornaTokenNoNulo() {
        // Act
        String token = jwtUtil.generateToken(userAdmin);

        // Assert
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void testGenerateToken_TokenTieneFormatoJwt() {
        // Act
        String token = jwtUtil.generateToken(userAdmin);

        // Assert — un JWT siempre tiene 3 partes separadas por punto
        String[] partes = token.split("\\.");
        assertEquals(3, partes.length, "El token JWT debe tener header.payload.signature");
    }

    @Test
    void testGenerateToken_DistintosUsuariosGeneranTokensDistintos() {
        // Act
        String tokenAdmin    = jwtUtil.generateToken(userAdmin);
        String tokenEmpleado = jwtUtil.generateToken(userEmpleado);
        String tokenCliente  = jwtUtil.generateToken(userCliente);

        // Assert
        assertNotEquals(tokenAdmin, tokenEmpleado);
        assertNotEquals(tokenAdmin, tokenCliente);
        assertNotEquals(tokenEmpleado, tokenCliente);
    }

    // Extracción de username

    @Test
    void testExtractUsername_RetornaUsernameDeAdmin() {
        // Arrange
        String token = jwtUtil.generateToken(userAdmin);

        // Act
        String username = jwtUtil.extractUsername(token);

        // Assert
        assertEquals("admin", username);
    }

    @Test
    void testExtractUsername_RetornaUsernameDeEmpleado() {
        // Arrange
        String token = jwtUtil.generateToken(userEmpleado);

        // Act
        String username = jwtUtil.extractUsername(token);

        // Assert
        assertEquals("empleado", username);
    }

    @Test
    void testExtractUsername_RetornaUsernameDeCliente() {
        // Arrange
        String token = jwtUtil.generateToken(userCliente);

        // Act
        String username = jwtUtil.extractUsername(token);

        // Assert
        assertEquals("cliente", username);
    }

    // Extracción de roles

    @Test
    void testExtractRoles_AdminContieneRoleAdmin() {
        // Arrange
        String token = jwtUtil.generateToken(userAdmin);

        // Act
        List<String> roles = jwtUtil.extractRoles(token);

        // Assert
        assertNotNull(roles);
        assertFalse(roles.isEmpty());
        assertTrue(roles.contains("ROLE_ADMIN"));
    }

    @Test
    void testExtractRoles_EmpleadoContieneRoleEmpleado() {
        // Arrange
        String token = jwtUtil.generateToken(userEmpleado);

        // Act
        List<String> roles = jwtUtil.extractRoles(token);

        // Assert
        assertNotNull(roles);
        assertTrue(roles.contains("ROLE_EMPLEADO"));
    }

    @Test
    void testExtractRoles_ClienteContieneRoleCliente() {
        // Arrange
        String token = jwtUtil.generateToken(userCliente);

        // Act
        List<String> roles = jwtUtil.extractRoles(token);

        // Assert
        assertNotNull(roles);
        assertTrue(roles.contains("ROLE_CLIENTE"));
    }

    @Test
    void testExtractRoles_AdminNoContieneRoleEmpleado() {
        // Arrange
        String token = jwtUtil.generateToken(userAdmin);

        // Act
        List<String> roles = jwtUtil.extractRoles(token);

        // Assert
        assertFalse(roles.contains("ROLE_EMPLEADO"));
    }

    // Validación de token

    @Test
    void testValidateToken_TokenValidoParaAdmin_RetornaTrue() {
        // Arrange
        String token = jwtUtil.generateToken(userAdmin);

        // Act
        boolean valido = jwtUtil.validateToken(token, userAdmin);

        // Assert
        assertTrue(valido);
    }

    @Test
    void testValidateToken_TokenDeAdminContraEmpleado_RetornaFalse() {
        // Arrange — token generado para admin pero validado contra empleado
        String token = jwtUtil.generateToken(userAdmin);

        // Act
        boolean valido = jwtUtil.validateToken(token, userEmpleado);

        // Assert
        assertFalse(valido);
    }

    @Test
    void testValidateToken_TokenDeEmpleadoContraCliente_RetornaFalse() {
        // Arrange
        String token = jwtUtil.generateToken(userEmpleado);

        // Act
        boolean valido = jwtUtil.validateToken(token, userCliente);

        // Assert
        assertFalse(valido);
    }

    @Test
    void testValidateToken_TokenDeClienteEsValidoParaCliente() {
        // Arrange
        String token = jwtUtil.generateToken(userCliente);

        // Act
        boolean valido = jwtUtil.validateToken(token, userCliente);

        // Assert
        assertTrue(valido);
    }

    // Token inválido / malformado

    @Test
    void testExtractUsername_TokenMalformado_LanzaExcepcion() {
        // Arrange
        String tokenInvalido = "esto.no.esunjwt";

        // Act & Assert
        assertThrows(Exception.class, () -> jwtUtil.extractUsername(tokenInvalido));
    }

    @Test
    void testExtractUsername_TokenVacio_LanzaExcepcion() {
        // Act & Assert
        assertThrows(Exception.class, () -> jwtUtil.extractUsername(""));
    }

    @Test
    void testExtractRoles_TokenManipulado_LanzaExcepcion() {
        // Arrange — token con firma alterada
        String token = jwtUtil.generateToken(userAdmin);
        String tokenManipulado = token.substring(0, token.lastIndexOf('.') + 1) + "firmaFalsa";

        // Act & Assert
        assertThrows(Exception.class, () -> jwtUtil.extractRoles(tokenManipulado));
    }
}