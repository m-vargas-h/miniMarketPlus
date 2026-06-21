package com.minimarket.service.impl;

import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.repository.InventarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceImplTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @InjectMocks
    private InventarioServiceImpl inventarioService;

    private Producto producto;
    private Inventario inventario;

    @BeforeEach
    void setUp() {
        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Leche");
        producto.setPrecio(990.0);
        producto.setStock(50);

        inventario = new Inventario();
        inventario.setId(1L);
        inventario.setProducto(producto);
        inventario.setCantidad(20);
        inventario.setTipoMovimiento("Entrada");
        inventario.setFechaMovimiento(new Date());
    }

    // Pruebas de información de movimiento

    @Test
    void testInventario_TipoMovimientoNoEsNulo() {
        // Arrange
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));

        // Act
        Inventario resultado = inventarioService.findById(1L);

        // Assert
        assertNotNull(resultado.getTipoMovimiento());
        assertFalse(resultado.getTipoMovimiento().isBlank());
    }

    @Test
    void testInventario_CantidadNoEsNula() {
        // Arrange
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));

        // Act
        Inventario resultado = inventarioService.findById(1L);

        // Assert
        assertNotNull(resultado.getCantidad());
        assertTrue(resultado.getCantidad() > 0);
    }

    @Test
    void testInventario_TipoMovimientoEsValido() {
        // Arrange
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));

        // Act
        Inventario resultado = inventarioService.findById(1L);

        // Assert
        String tipo = resultado.getTipoMovimiento();
        assertTrue(
            tipo.equals("Entrada") || tipo.equals("Salida"),
            "El tipo de movimiento debe ser 'Entrada' o 'Salida'"
        );
    }

    // Pruebas de relación Producto-Inventario

    @Test
    void testInventario_ProductoAsociadoEsElCorrecto() {
        // Arrange
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));

        // Act
        Inventario resultado = inventarioService.findById(1L);

        // Assert
        assertNotNull(resultado.getProducto());
        assertEquals(1L, resultado.getProducto().getId());
        assertEquals("Leche", resultado.getProducto().getNombre());
    }

    @Test
    void testInventario_ProductoNoEsNulo() {
        // Arrange
        when(inventarioRepository.findByProductoId(1L)).thenReturn(List.of(inventario));

        // Act
        List<Inventario> resultado = inventarioService.findByProductoId(1L);

        // Assert
        assertFalse(resultado.isEmpty());
        assertNotNull(resultado.get(0).getProducto());
        assertEquals("Leche", resultado.get(0).getProducto().getNombre());
    }

    // Pruebas de comportamiento del servicio

    @Test
    void testFindById_InventarioExistente() {
        // Arrange
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));

        // Act
        Inventario resultado = inventarioService.findById(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void testFindById_InventarioNoExistente() {
        // Arrange
        when(inventarioRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Inventario resultado = inventarioService.findById(99L);

        // Assert
        assertNull(resultado);
    }

    @Test
    void testFindAll_RetornaListaDeInventarios() {
        // Arrange
        when(inventarioRepository.findAll()).thenReturn(List.of(inventario));

        // Act
        List<Inventario> resultado = inventarioService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
    }

    @Test
    void testSave_InvocaRepositorioUnaVez() {
        // Arrange
        when(inventarioRepository.save(inventario)).thenReturn(inventario);

        // Act
        Inventario resultado = inventarioService.save(inventario);

        // Assert
        assertNotNull(resultado);
        assertEquals("Entrada", resultado.getTipoMovimiento());
        verify(inventarioRepository, times(1)).save(inventario);
    }
}