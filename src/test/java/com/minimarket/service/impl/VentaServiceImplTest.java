package com.minimarket.service.impl;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.repository.VentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceImplTest {

    @Mock
    private VentaRepository ventaRepository;

    @InjectMocks
    private VentaServiceImpl ventaService;

    private Usuario usuario;
    private Venta venta;
    private Producto producto1;
    private Producto producto2;
    private DetalleVenta detalle1;
    private DetalleVenta detalle2;

    @BeforeEach
    void setUp() {
        // Usuario con rol EMPLEADO
        Rol rolEmpleado = new Rol();
        rolEmpleado.setId(2L);
        rolEmpleado.setNombre("ROLE_EMPLEADO");

        Set<Rol> roles = new HashSet<>();
        roles.add(rolEmpleado);

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("empleado");
        usuario.setPassword("$2a$10$hasheado");
        usuario.setRoles(roles);

        // Productos con stock
        producto1 = new Producto();
        producto1.setId(1L);
        producto1.setNombre("Arroz");
        producto1.setPrecio(1500.0);
        producto1.setStock(10);

        producto2 = new Producto();
        producto2.setId(2L);
        producto2.setNombre("Aceite");
        producto2.setPrecio(2500.0);
        producto2.setStock(5);

        // Detalles de venta
        detalle1 = new DetalleVenta();
        detalle1.setId(1L);
        detalle1.setProducto(producto1);
        detalle1.setCantidad(2);
        detalle1.setPrecio(producto1.getPrecio());

        detalle2 = new DetalleVenta();
        detalle2.setId(2L);
        detalle2.setProducto(producto2);
        detalle2.setCantidad(1);
        detalle2.setPrecio(producto2.getPrecio());

        // Venta
        venta = new Venta();
        venta.setId(1L);
        venta.setUsuario(usuario);
        venta.setFecha(new Date());
        venta.setDetalles(List.of(detalle1, detalle2));

        // Enlazar detalles a la venta
        detalle1.setVenta(venta);
        detalle2.setVenta(venta);
    }

    // comportamiento del servicio

    @Test
    void testFindAll_RetornaListaDeVentas() {
        // Arrange
        when(ventaRepository.findAll()).thenReturn(List.of(venta));

        // Act
        List<Venta> resultado = ventaService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
    }

    @Test
    void testFindById_VentaExistente() {
        // Arrange
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));

        // Act
        Venta resultado = ventaService.findById(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void testFindById_VentaNoExistente() {
        // Arrange
        when(ventaRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Venta resultado = ventaService.findById(99L);

        // Assert
        assertNull(resultado);
    }

    @Test
    void testFindByUsuarioId_RetornaVentasDelUsuario() {
        // Arrange
        when(ventaRepository.findByUsuarioId(1L)).thenReturn(List.of(venta));

        // Act
        List<Venta> resultado = ventaService.findByUsuarioId(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getUsuario().getId());
    }

    // pruebas que validan relaciones entre objetos del modelo

    @Test
    void testVenta_TieneUsuarioAsociado() {
        // Arrange
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));

        // Act
        Venta resultado = ventaService.findById(1L);

        // Assert
        assertNotNull(resultado.getUsuario());
        assertEquals("empleado", resultado.getUsuario().getUsername());
    }

    @Test
    void testDetalle_TieneProductoAsociado() {
        // Arrange
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));

        // Act
        Venta resultado = ventaService.findById(1L);

        // Assert
        assertNotNull(resultado.getDetalles());
        assertFalse(resultado.getDetalles().isEmpty());
        assertNotNull(resultado.getDetalles().get(0).getProducto());
        assertEquals("Arroz", resultado.getDetalles().get(0).getProducto().getNombre());
    }

    // pruebas que validan lógica interna

    @Test
    void testCalcularTotalVenta_SumaCorrectamente() {
        // Arrange
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));

        // Act
        Venta resultado = ventaService.findById(1L);
        double total = resultado.getDetalles().stream()
                .mapToDouble(d -> d.getPrecio() * d.getCantidad())
                .sum();

        // Assert
        // detalle1: 1500.0 * 2 = 3000.0
        // detalle2: 2500.0 * 1 = 2500.0
        // total esperado: 5500.0
        assertEquals(5500.0, total, 0.01);
    }

    @Test
    void testProducto_StockSuficienteParaVenta() {
        // Arrange
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));

        // Act
        Venta resultado = ventaService.findById(1L);
        boolean stockSuficiente = resultado.getDetalles().stream()
                .allMatch(d -> d.getProducto().getStock() >= d.getCantidad());

        // Assert
        assertTrue(stockSuficiente);
    }

    @Test
    void testProducto_StockInsuficienteParaVenta() {
        // Arrange
        producto1.setStock(1); // solo 1 en stock, pero la venta pide 2
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));

        // Act
        Venta resultado = ventaService.findById(1L);
        boolean stockSuficiente = resultado.getDetalles().stream()
                .allMatch(d -> d.getProducto().getStock() >= d.getCantidad());

        // Assert
        assertFalse(stockSuficiente);
    }

    // pruebas que validan interacción con el repositorio

    @Test
    void testSave_InvocaRepositorioUnaVez() {
        // Arrange
        when(ventaRepository.save(venta)).thenReturn(venta);

        // Act
        Venta resultado = ventaService.save(venta);

        // Assert
        assertNotNull(resultado);
        verify(ventaRepository, times(1)).save(venta);
    }
}