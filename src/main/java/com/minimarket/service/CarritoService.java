package com.minimarket.service;

import com.minimarket.entity.Carrito;

import java.util.List;

public interface CarritoService {
    List<Carrito> findAll();
    Carrito findById(Long id);
    Carrito agregarProducto(Carrito carrito);
    Carrito save(Carrito carrito);
    void deleteById(Long id);
    List<Carrito> findByUsuarioId(Long usuarioId);
}
