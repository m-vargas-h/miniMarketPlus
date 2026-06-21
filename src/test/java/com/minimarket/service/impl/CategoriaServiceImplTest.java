package com.minimarket.service.impl;

import com.minimarket.entity.Categoria;
import com.minimarket.repository.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceImplTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaServiceImpl categoriaService;

    private Categoria categoria;

    @BeforeEach
    void setUp() {
        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Lácteos");
    }

    @Test
    void testFindAll_RetornaListaDeCategorias() {
        // Arrange
        when(categoriaRepository.findAll()).thenReturn(List.of(categoria));

        // Act
        List<Categoria> resultado = categoriaService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Lácteos", resultado.get(0).getNombre());
    }

    @Test
    void testFindById_CategoriaExistente() {
        // Arrange
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        // Act
        Categoria resultado = categoriaService.findById(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Lácteos", resultado.getNombre());
    }

    @Test
    void testFindById_CategoriaNoExistente() {
        // Arrange
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Categoria resultado = categoriaService.findById(99L);

        // Assert
        assertNull(resultado);
    }

    @Test
    void testSave_GuardaCategoria() {
        // Arrange
        when(categoriaRepository.save(categoria)).thenReturn(categoria);

        // Act
        Categoria resultado = categoriaService.save(categoria);

        // Assert
        assertNotNull(resultado);
        assertEquals("Lácteos", resultado.getNombre());
        verify(categoriaRepository, times(1)).save(categoria);
    }

    @Test
    void testDeleteById_InvocaRepositorioUnaVez() {
        // Arrange
        doNothing().when(categoriaRepository).deleteById(1L);

        // Act
        categoriaService.deleteById(1L);

        // Assert
        verify(categoriaRepository, times(1)).deleteById(1L);
    }

    @Test
    void testCategoria_NombreNoEsNulo() {
        // Arrange
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        // Act
        Categoria resultado = categoriaService.findById(1L);

        // Assert
        assertNotNull(resultado.getNombre());
        assertFalse(resultado.getNombre().isBlank());
    }
}