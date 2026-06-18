package com.minimarket.entity;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    // ─── Usuario ───

    @Test
    void testUsuario_GettersYSetters() {
        Rol rol = new Rol();
        rol.setId(1L);
        rol.setNombre("ROLE_ADMIN");

        Set<Rol> roles = new HashSet<>();
        roles.add(rol);

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("admin");
        usuario.setPassword("pass123");
        usuario.setRoles(roles);

        assertEquals(1L, usuario.getId());
        assertEquals("admin", usuario.getUsername());
        assertEquals("pass123", usuario.getPassword());
        assertEquals(1, usuario.getRoles().size());
    }

    // ─── Rol ───

    @Test
    void testRol_GettersYSetters() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("admin");

        Set<Usuario> usuarios = new HashSet<>();
        usuarios.add(usuario);

        Rol rol = new Rol();
        rol.setId(1L);
        rol.setNombre("ROLE_ADMIN");
        rol.setUsuarios(usuarios);

        assertEquals(1L, rol.getId());
        assertEquals("ROLE_ADMIN", rol.getNombre());
        assertEquals(1, rol.getUsuarios().size());
    }

    // ─── Categoria ───

    @Test
    void testCategoria_GettersYSetters() {
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Lácteos");

        assertEquals(1L, categoria.getId());
        assertEquals("Lácteos", categoria.getNombre());
    }

    // ─── Producto ───

    @Test
    void testProducto_GettersYSetters() {
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Lácteos");

        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Leche");
        producto.setPrecio(990.0);
        producto.setStock(20);
        producto.setCategoria(categoria);

        assertEquals(1L, producto.getId());
        assertEquals("Leche", producto.getNombre());
        assertEquals(990.0, producto.getPrecio());
        assertEquals(20, producto.getStock());
        assertEquals("Lácteos", producto.getCategoria().getNombre());
    }

    // ─── Inventario ───

    @Test
    void testInventario_GettersYSetters() {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Leche");

        Date fecha = new Date();

        Inventario inventario = new Inventario();
        inventario.setId(1L);
        inventario.setProducto(producto);
        inventario.setCantidad(50);
        inventario.setTipoMovimiento("Entrada");
        inventario.setFechaMovimiento(fecha);

        assertEquals(1L, inventario.getId());
        assertEquals("Leche", inventario.getProducto().getNombre());
        assertEquals(50, inventario.getCantidad());
        assertEquals("Entrada", inventario.getTipoMovimiento());
        assertEquals(fecha, inventario.getFechaMovimiento());
    }

    // ─── Venta ───

    @Test
    void testVenta_GettersYSetters() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("empleado");

        Date fecha = new Date();

        Venta venta = new Venta();
        venta.setId(1L);
        venta.setUsuario(usuario);
        venta.setFecha(fecha);
        venta.setDetalles(List.of());

        assertEquals(1L, venta.getId());
        assertEquals("empleado", venta.getUsuario().getUsername());
        assertEquals(fecha, venta.getFecha());
        assertNotNull(venta.getDetalles());
    }

    // ─── DetalleVenta ───

    @Test
    void testDetalleVenta_GettersYSetters() {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Arroz");
        producto.setPrecio(1500.0);

        Venta venta = new Venta();
        venta.setId(1L);

        DetalleVenta detalle = new DetalleVenta();
        detalle.setId(1L);
        detalle.setVenta(venta);
        detalle.setProducto(producto);
        detalle.setCantidad(3);
        detalle.setPrecio(1500.0);

        assertEquals(1L, detalle.getId());
        assertEquals(1L, detalle.getVenta().getId());
        assertEquals("Arroz", detalle.getProducto().getNombre());
        assertEquals(3, detalle.getCantidad());
        assertEquals(1500.0, detalle.getPrecio());
    }

    // ─── Carrito ───

    @Test
    void testCarrito_GettersYSetters() {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Aceite");

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("cliente");

        Carrito carrito = new Carrito();
        carrito.setId(1L);
        carrito.setUsuario(usuario);
        carrito.setProducto(producto);
        carrito.setCantidad(2);

        assertEquals(1L, carrito.getId());
        assertEquals("cliente", carrito.getUsuario().getUsername());
        assertEquals("Aceite", carrito.getProducto().getNombre());
        assertEquals(2, carrito.getCantidad());
    }
}