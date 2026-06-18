package com.minimarket.service.impl;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuario;
    private Rol rolAdmin;

    @BeforeEach
    void setUp() {
        rolAdmin = new Rol();
        rolAdmin.setId(1L);
        rolAdmin.setNombre("ROLE_ADMIN");

        Set<Rol> roles = new HashSet<>();
        roles.add(rolAdmin);

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("admin");
        usuario.setPassword("$2a$10$hasheado");
        usuario.setRoles(roles);
    }

    // pruebas que validan datos del usuario

    @Test
    void testUsuario_UsernameNoEsNulo() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // Act
        Optional<Usuario> resultado = usuarioService.findById(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertNotNull(resultado.get().getUsername());
    }

    @Test
    void testUsuario_PasswordNoEsNula() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // Act
        Optional<Usuario> resultado = usuarioService.findById(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertNotNull(resultado.get().getPassword());
    }

    @Test
    void testUsuario_RolesNoEstaVacio() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // Act
        Optional<Usuario> resultado = usuarioService.findById(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertFalse(resultado.get().getRoles().isEmpty());
    }

    @Test
    void testUsuario_TieneRolEsperado() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // Act
        Optional<Usuario> resultado = usuarioService.findById(1L);

        // Assert
        assertTrue(resultado.isPresent());
        boolean tieneRolAdmin = resultado.get().getRoles().stream()
                .anyMatch(r -> r.getNombre().equals("ROLE_ADMIN"));
        assertTrue(tieneRolAdmin);
    }

    // pruebas que validan comportamiento del servicio

    @Test
    void testFindByUsername_UsuarioExistente() {
        // Arrange
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));

        // Act
        Optional<Usuario> resultado = usuarioService.findByUsername("admin");

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("admin", resultado.get().getUsername());
    }

    @Test
    void testFindByUsername_UsuarioNoExistente() {
        // Arrange
        when(usuarioRepository.findByUsername("fantasma")).thenReturn(Optional.empty());

        // Act
        Optional<Usuario> resultado = usuarioService.findByUsername("fantasma");

        // Assert
        assertFalse(resultado.isPresent());
    }

    @Test
    void testFindAll_RetornaListaDeUsuarios() {
        // Arrange
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

        // Act
        List<Usuario> resultado = usuarioService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
    }

    // pruebas que validan interacción con el repositorio

    @Test
    void testSave_InvocaRepositorioUnaVez() {
        // Arrange
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        // Act
        Usuario resultado = usuarioService.save(usuario);

        // Assert
        assertNotNull(resultado);
        assertEquals("admin", resultado.getUsername());
        verify(usuarioRepository, times(1)).save(usuario);
    }
}