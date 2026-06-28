package com.minimarket.security.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtUtil — Pruebas unitarias de generación, extracción y validación de tokens JWT")
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
    @DisplayName("Token generado para admin no es nulo ni vacío")
    void generateToken_adminRetornaTokenValido() {
        // Act
        String token = jwtUtil.generateToken(userAdmin);

        // Assert
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    @DisplayName("Token tiene formato JWT de tres partes separadas por punto")
    void generateToken_tieneFormatoJwt() {
        // Act
        String token = jwtUtil.generateToken(userAdmin);

        // Assert — un JWT siempre tiene header.payload.signature
        String[] partes = token.split("\\.");
        assertEquals(3, partes.length, "El token JWT debe tener header.payload.signature");
    }

    @Test
    @DisplayName("Usuarios distintos generan tokens distintos")
    void generateToken_usuariosDistintosGeneranTokensDistintos() {
        // Act
        String tokenAdmin    = jwtUtil.generateToken(userAdmin);
        String tokenEmpleado = jwtUtil.generateToken(userEmpleado);
        String tokenCliente  = jwtUtil.generateToken(userCliente);

        // Assert — cada token debe ser único por usuario
        assertNotEquals(tokenAdmin, tokenEmpleado);
        assertNotEquals(tokenAdmin, tokenCliente);
        assertNotEquals(tokenEmpleado, tokenCliente);
    }

    // Extracción de username

    @Test
    @DisplayName("Extrae username 'admin' del token de admin")
    void extractUsername_retornaUsernameAdmin() {
        // Arrange
        String token = jwtUtil.generateToken(userAdmin);

        // Act
        String username = jwtUtil.extractUsername(token);

        // Assert
        assertEquals("admin", username);
    }

    @Test
    @DisplayName("Extrae username 'empleado' del token de empleado")
    void extractUsername_retornaUsernameEmpleado() {
        // Arrange
        String token = jwtUtil.generateToken(userEmpleado);

        // Act
        String username = jwtUtil.extractUsername(token);

        // Assert
        assertEquals("empleado", username);
    }

    @Test
    @DisplayName("Extrae username 'cliente' del token de cliente")
    void extractUsername_retornaUsernameCliente() {
        // Arrange
        String token = jwtUtil.generateToken(userCliente);

        // Act
        String username = jwtUtil.extractUsername(token);

        // Assert
        assertEquals("cliente", username);
    }

    // Extracción de roles

    @Test
    @DisplayName("Token de admin contiene ROLE_ADMIN en los claims")
    void extractRoles_adminContieneRoleAdmin() {
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
    @DisplayName("Token de empleado contiene ROLE_EMPLEADO en los claims")
    void extractRoles_empleadoContieneRoleEmpleado() {
        // Arrange
        String token = jwtUtil.generateToken(userEmpleado);

        // Act
        List<String> roles = jwtUtil.extractRoles(token);

        // Assert
        assertNotNull(roles);
        assertTrue(roles.contains("ROLE_EMPLEADO"));
    }

    @Test
    @DisplayName("Token de cliente contiene ROLE_CLIENTE en los claims")
    void extractRoles_clienteContieneRoleCliente() {
        // Arrange
        String token = jwtUtil.generateToken(userCliente);

        // Act
        List<String> roles = jwtUtil.extractRoles(token);

        // Assert
        assertNotNull(roles);
        assertTrue(roles.contains("ROLE_CLIENTE"));
    }

    @Test
    @DisplayName("Token de admin no contiene roles de otros perfiles")
    void extractRoles_adminNoContieneRoleEmpleado() {
        // Arrange
        String token = jwtUtil.generateToken(userAdmin);

        // Act
        List<String> roles = jwtUtil.extractRoles(token);

        // Assert — los claims no deben mezclar roles entre usuarios
        assertFalse(roles.contains("ROLE_EMPLEADO"));
    }

    // Validación de token

    @Test
    @DisplayName("Token válido para su propio usuario retorna true")
    void validateToken_tokenValidoParaAdmin() {
        // Arrange
        String token = jwtUtil.generateToken(userAdmin);

        // Act
        boolean valido = jwtUtil.validateToken(token, userAdmin);

        // Assert
        assertTrue(valido);
    }

    @Test
    @DisplayName("Token de admin rechazado al validar contra empleado")
    void validateToken_tokenAdminContraEmpleadoRetornaFalse() {
        // Arrange — token generado para admin, validado contra empleado
        String token = jwtUtil.generateToken(userAdmin);

        // Act
        boolean valido = jwtUtil.validateToken(token, userEmpleado);

        // Assert
        assertFalse(valido);
    }

    @Test
    @DisplayName("Token de empleado rechazado al validar contra cliente")
    void validateToken_tokenEmpleadoContraClienteRetornaFalse() {
        // Arrange
        String token = jwtUtil.generateToken(userEmpleado);

        // Act
        boolean valido = jwtUtil.validateToken(token, userCliente);

        // Assert
        assertFalse(valido);
    }

    @Test
    @DisplayName("Token de cliente es válido para el propio cliente")
    void validateToken_tokenClienteValidoParaCliente() {
        // Arrange
        String token = jwtUtil.generateToken(userCliente);

        // Act
        boolean valido = jwtUtil.validateToken(token, userCliente);

        // Assert
        assertTrue(valido);
    }

    // Token inválido 

    @Test
    @DisplayName("Token malformado lanza excepción al extraer username")
    void extractUsername_tokenMalformadoLanzaExcepcion() {
        // Arrange
        String tokenInvalido = "esto.no.esunjwt";

        // Act & Assert — token con estructura inválida debe ser rechazado
        assertThrows(Exception.class, () -> jwtUtil.extractUsername(tokenInvalido));
    }

    @Test
    @DisplayName("Token vacío lanza excepción al extraer username")
    void extractUsername_tokenVacioLanzaExcepcion() {
        // Act & Assert
        assertThrows(Exception.class, () -> jwtUtil.extractUsername(""));
    }

    @Test
    @DisplayName("Token con firma alterada lanza excepción al extraer roles")
    void extractRoles_tokenManipuladoLanzaExcepcion() {
        // Arrange — se reemplaza la firma por una cadena falsa
        String token = jwtUtil.generateToken(userAdmin);
        String tokenManipulado = token.substring(0, token.lastIndexOf('.') + 1) + "firmaFalsa";

        // Act & Assert — firma inválida debe ser detectada y rechazada
        assertThrows(Exception.class, () -> jwtUtil.extractRoles(tokenManipulado));
    }
}