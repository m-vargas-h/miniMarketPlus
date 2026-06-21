package com.minimarket.service.impl;

import com.minimarket.entity.Rol;
import com.minimarket.repository.RolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolServiceImplTest {

    @Mock
    private RolRepository rolRepository;

    @InjectMocks
    private RolServiceImpl rolService;

    private Rol rol;

    @BeforeEach
    void setUp() {
        rol = new Rol();
        rol.setId(1L);
        rol.setNombre("ROLE_ADMIN");
    }

    // Test para el método findByNombre
    @Test
    void testFindByNombre_RolExistente() {
        // Arrange
        when(rolRepository.findByNombre("ROLE_ADMIN")).thenReturn(Optional.of(rol));

        // Act
        Optional<Rol> resultado = rolService.findByNombre("ROLE_ADMIN");

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("ROLE_ADMIN", resultado.get().getNombre());
    }

    // Test para el método findByNombre con rol no existente
    @Test
    void testFindByNombre_RolNoExistente() {
        // Arrange
        when(rolRepository.findByNombre("ROLE_INEXISTENTE")).thenReturn(Optional.empty());

        // Act
        Optional<Rol> resultado = rolService.findByNombre("ROLE_INEXISTENTE");

        // Assert
        assertFalse(resultado.isPresent());
    }

    // Test para verificar que el nombre del rol no es nulo ni vacío
    @Test
    void testRol_NombreNoEsNulo() {
        // Arrange
        when(rolRepository.findByNombre("ROLE_ADMIN")).thenReturn(Optional.of(rol));

        // Act
        Optional<Rol> resultado = rolService.findByNombre("ROLE_ADMIN");

        // Assert
        assertTrue(resultado.isPresent());
        assertNotNull(resultado.get().getNombre());
        assertFalse(resultado.get().getNombre().isBlank());
    }
}