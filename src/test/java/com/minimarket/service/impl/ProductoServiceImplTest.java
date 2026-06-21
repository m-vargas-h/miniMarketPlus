package com.minimarket.service.impl;

import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.repository.ProductoRepository;
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
class ProductoServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoServiceImpl productoService;

    private Categoria categoria;
    private Producto producto;

    @BeforeEach
    void setUp() {
        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Lácteos");

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Leche");
        producto.setPrecio(990.0);
        producto.setStock(20);
        producto.setCategoria(categoria);
    }

    // Test para el método findAll
    @Test
    void testFindAll_RetornaListaDeProductos() {
        // Arrange
        when(productoRepository.findAll()).thenReturn(List.of(producto));

        // Act
        List<Producto> resultado = productoService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Leche", resultado.get(0).getNombre());
    }

    // Test para el método findById
    @Test
    void testFindById_ProductoExistente() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        // Act
        Producto resultado = productoService.findById(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Leche", resultado.getNombre());
    }

    // Test para el método findById con producto no existente
    @Test
    void testFindById_ProductoNoExistente() {
        // Arrange
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Producto resultado = productoService.findById(99L);

        // Assert
        assertNull(resultado);
    }

    // Test para el método save
    @Test
    void testSave_GuardaProducto() {
        // Arrange
        when(productoRepository.save(producto)).thenReturn(producto);

        // Act
        Producto resultado = productoService.save(producto);

        // Assert
        assertNotNull(resultado);
        assertEquals("Leche", resultado.getNombre());
        verify(productoRepository, times(1)).save(producto);
    }

    // Test para el método deleteById
    @Test
    void testDeleteById_InvocaRepositorioUnaVez() {
        // Arrange
        doNothing().when(productoRepository).deleteById(1L);

        // Act
        productoService.deleteById(1L);

        // Assert
        verify(productoRepository, times(1)).deleteById(1L);
    }

    // Test para el método findByCategoriaId
    @Test
    void testFindByCategoriaId_RetornaProductosDeLaCategoria() {
        // Arrange
        when(productoRepository.findByCategoriaId(1L)).thenReturn(List.of(producto));

        // Act
        List<Producto> resultado = productoService.findByCategoriaId(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Lácteos", resultado.get(0).getCategoria().getNombre());
    }

    // Test para verificar que el producto tiene una categoría asociada
    @Test
    void testProducto_CategoriaAsociadaEsLaCorrecta() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        // Act
        Producto resultado = productoService.findById(1L);

        // Assert
        assertNotNull(resultado.getCategoria());
        assertEquals(1L, resultado.getCategoria().getId());
        assertEquals("Lácteos", resultado.getCategoria().getNombre());
    }
}