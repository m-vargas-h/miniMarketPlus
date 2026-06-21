package com.minimarket.service.impl;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Venta;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.DetalleVentaRepository;
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
class DetalleVentaServiceImplTest {

    @Mock
    private DetalleVentaRepository detalleVentaRepository;

    @InjectMocks
    private DetalleVentaServiceImpl detalleVentaService;

    private Producto producto;
    private Venta venta;
    private DetalleVenta detalle;

    @BeforeEach
    void setUp() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("empleado");
        usuario.setPassword("$2a$10$hasheado");

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Arroz");
        producto.setPrecio(1500.0);
        producto.setStock(10);

        venta = new Venta();
        venta.setId(1L);
        venta.setUsuario(usuario);
        venta.setFecha(new Date());

        detalle = new DetalleVenta();
        detalle.setId(1L);
        detalle.setVenta(venta);
        detalle.setProducto(producto);
        detalle.setCantidad(2);
        detalle.setPrecio(1500.0);
    }

    @Test
    void testFindAll_RetornaListaDeDetalles() {
        // Arrange
        when(detalleVentaRepository.findAll()).thenReturn(List.of(detalle));

        // Act
        List<DetalleVenta> resultado = detalleVentaService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
    }

    @Test
    void testFindById_DetalleExistente() {
        // Arrange
        when(detalleVentaRepository.findById(1L)).thenReturn(Optional.of(detalle));

        // Act
        DetalleVenta resultado = detalleVentaService.findById(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void testFindById_DetalleNoExistente() {
        // Arrange
        when(detalleVentaRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        DetalleVenta resultado = detalleVentaService.findById(99L);

        // Assert
        assertNull(resultado);
    }

    @Test
    void testSave_GuardaDetalle() {
        // Arrange
        when(detalleVentaRepository.save(detalle)).thenReturn(detalle);

        // Act
        DetalleVenta resultado = detalleVentaService.save(detalle);

        // Assert
        assertNotNull(resultado);
        assertEquals(1500.0, resultado.getPrecio());
        verify(detalleVentaRepository, times(1)).save(detalle);
    }

    @Test
    void testDeleteById_InvocaRepositorioUnaVez() {
        // Arrange
        doNothing().when(detalleVentaRepository).deleteById(1L);

        // Act
        detalleVentaService.deleteById(1L);

        // Assert
        verify(detalleVentaRepository, times(1)).deleteById(1L);
    }

    @Test
    void testFindByVentaId_RetornaDetallesDeLaVenta() {
        // Arrange
        when(detalleVentaRepository.findByVentaId(1L)).thenReturn(List.of(detalle));

        // Act
        List<DetalleVenta> resultado = detalleVentaService.findByVentaId(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getVenta().getId());
    }

    @Test
    void testDetalle_ProductoAsociadoEsElCorrecto() {
        // Arrange
        when(detalleVentaRepository.findById(1L)).thenReturn(Optional.of(detalle));

        // Act
        DetalleVenta resultado = detalleVentaService.findById(1L);

        // Assert
        assertNotNull(resultado.getProducto());
        assertEquals(1L, resultado.getProducto().getId());
        assertEquals("Arroz", resultado.getProducto().getNombre());
    }
}