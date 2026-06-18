package com.minimarket.service.impl;

import com.minimarket.entity.Carrito;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.CarritoRepository;
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
class CarritoServiceImplTest {

    @Mock
    private CarritoRepository carritoRepository;

    @InjectMocks
    private CarritoServiceImpl carritoService;

    private Usuario usuario;
    private Producto producto;
    private Carrito carrito;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("cliente");
        usuario.setPassword("$2a$10$hasheado");

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Arroz");
        producto.setPrecio(1500.0);
        producto.setStock(10);

        carrito = new Carrito();
        carrito.setId(1L);
        carrito.setUsuario(usuario);
        carrito.setProducto(producto);
        carrito.setCantidad(3);
    }

    // ─── Pruebas de disponibilidad de stock ───

    @Test
    void testAgregarProducto_StockSuficiente_GuardaCarrito() {
        // Arrange
        when(carritoRepository.save(carrito)).thenReturn(carrito);

        // Act
        Carrito resultado = carritoService.agregarProducto(carrito);

        // Assert
        assertNotNull(resultado);
        assertEquals(3, resultado.getCantidad());
        assertEquals(7, resultado.getProducto().getStock()); // 10 - 3 = 7
        verify(carritoRepository, times(1)).save(carrito);
    }

    @Test
    void testAgregarProducto_StockInsuficiente_LanzaExcepcion() {
        // Arrange
        carrito.setCantidad(15); // solicita más de lo disponible (stock = 10)

        // Act & Assert
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> carritoService.agregarProducto(carrito)
        );
        assertTrue(ex.getMessage().contains("Stock insuficiente"));
        verify(carritoRepository, never()).save(any());
    }

    // ─── Prueba de relación Producto-Usuario ───

    @Test
    void testCarrito_UsuarioAsociadoEsElCorrecto() {
        // Arrange
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));

        // Act
        Carrito resultado = carritoService.findById(1L);

        // Assert
        assertNotNull(resultado.getUsuario());
        assertEquals(1L, resultado.getUsuario().getId());
        assertEquals("cliente", resultado.getUsuario().getUsername());
    }

    @Test
    void testCarrito_UsuarioNoEsNulo() {
        // Arrange
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(List.of(carrito));

        // Act
        List<Carrito> resultado = carritoService.findByUsuarioId(1L);

        // Assert
        assertFalse(resultado.isEmpty());
        assertNotNull(resultado.get(0).getUsuario());
        assertEquals("cliente", resultado.get(0).getUsuario().getUsername());
    }

    // ─── Pruebas de comportamiento del servicio ───

    @Test
    void testFindById_CarritoExistente() {
        // Arrange
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));

        // Act
        Carrito resultado = carritoService.findById(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void testFindById_CarritoNoExistente() {
        // Arrange
        when(carritoRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Carrito resultado = carritoService.findById(99L);

        // Assert
        assertNull(resultado);
    }

    @Test
    void testFindAll_RetornaListaDeCarritos() {
        // Arrange
        when(carritoRepository.findAll()).thenReturn(List.of(carrito));

        // Act
        List<Carrito> resultado = carritoService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
    }

    @Test
    void testSave_InvocaRepositorioUnaVez() {
        // Arrange
        when(carritoRepository.save(carrito)).thenReturn(carrito);

        // Act
        Carrito resultado = carritoService.save(carrito);

        // Assert
        assertNotNull(resultado);
        verify(carritoRepository, times(1)).save(carrito);
    }
}
