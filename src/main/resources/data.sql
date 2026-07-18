SET @seed_needed = (SELECT CASE WHEN EXISTS(SELECT 1 FROM rol) THEN 0 ELSE 1 END);

-- 1. ROLES
INSERT INTO rol (nombre) SELECT 'ROLE_ADMIN' WHERE @seed_needed = 1;
INSERT INTO rol (nombre) SELECT 'ROLE_EMPLEADO' WHERE @seed_needed = 1;
INSERT INTO rol (nombre) SELECT 'ROLE_CLIENTE' WHERE @seed_needed = 1;

-- 2. USUARIOS
INSERT INTO usuario (username, password)
SELECT 'admin', '$2a$10$EeVxc3zewmmsgWpyAFizMuoQoLd6wLlqRjz8QvjzD3.2gl9vl21w6' WHERE @seed_needed = 1;
INSERT INTO usuario (username, password)
SELECT 'empleado', '$2a$10$w8IaAghVbCBP7QVjt37Mg.nj99qMDebPS5BRWZRMYno9h//9OXgwu' WHERE @seed_needed = 1;
INSERT INTO usuario (username, password)
SELECT 'cliente', '$2a$10$ZOKshDyLuehAyjf4nrCCtuIp6E0tyohTzAF4cy3ZORhpDino5gMTa' WHERE @seed_needed = 1;

-- 3. ASIGNACION DE ROLES
INSERT INTO usuario_roles (usuario_id, rol_id)
SELECT (SELECT id FROM usuario WHERE username = 'admin'),
       (SELECT id FROM rol WHERE nombre = 'ROLE_ADMIN')
WHERE @seed_needed = 1;

INSERT INTO usuario_roles (usuario_id, rol_id)
SELECT (SELECT id FROM usuario WHERE username = 'empleado'),
       (SELECT id FROM rol WHERE nombre = 'ROLE_EMPLEADO')
WHERE @seed_needed = 1;

INSERT INTO usuario_roles (usuario_id, rol_id)
SELECT (SELECT id FROM usuario WHERE username = 'cliente'),
       (SELECT id FROM rol WHERE nombre = 'ROLE_CLIENTE')
WHERE @seed_needed = 1;

-- 4. CATEGORIAS
INSERT INTO categoria (nombre) SELECT 'Abarrotes' WHERE @seed_needed = 1;
INSERT INTO categoria (nombre) SELECT 'Bebidas' WHERE @seed_needed = 1;
INSERT INTO categoria (nombre) SELECT 'Lácteos' WHERE @seed_needed = 1;
INSERT INTO categoria (nombre) SELECT 'Aseo y limpieza' WHERE @seed_needed = 1;

-- 5. PRODUCTOS
INSERT INTO producto (nombre, precio, stock, categoria_id)
SELECT 'Arroz 1kg', 1290.0, 100, (SELECT id FROM categoria WHERE nombre = 'Abarrotes') WHERE @seed_needed = 1;
INSERT INTO producto (nombre, precio, stock, categoria_id)
SELECT 'Fideos 400g', 990.0, 80, (SELECT id FROM categoria WHERE nombre = 'Abarrotes') WHERE @seed_needed = 1;
INSERT INTO producto (nombre, precio, stock, categoria_id)
SELECT 'Coca-Cola 1.5L', 1590.0, 60, (SELECT id FROM categoria WHERE nombre = 'Bebidas') WHERE @seed_needed = 1;
INSERT INTO producto (nombre, precio, stock, categoria_id)
SELECT 'Agua mineral 500ml', 690.0, 120, (SELECT id FROM categoria WHERE nombre = 'Bebidas') WHERE @seed_needed = 1;
INSERT INTO producto (nombre, precio, stock, categoria_id)
SELECT 'Leche entera 1L', 1190.0, 50, (SELECT id FROM categoria WHERE nombre = 'Lácteos') WHERE @seed_needed = 1;
INSERT INTO producto (nombre, precio, stock, categoria_id)
SELECT 'Yogurt natural', 790.0, 40, (SELECT id FROM categoria WHERE nombre = 'Lácteos') WHERE @seed_needed = 1;
INSERT INTO producto (nombre, precio, stock, categoria_id)
SELECT 'Detergente 1kg', 3490.0, 30, (SELECT id FROM categoria WHERE nombre = 'Aseo y limpieza') WHERE @seed_needed = 1;
INSERT INTO producto (nombre, precio, stock, categoria_id)
SELECT 'Cloro 1L', 1290.0, 45, (SELECT id FROM categoria WHERE nombre = 'Aseo y limpieza') WHERE @seed_needed = 1;

-- 6. INVENTARIO
INSERT INTO inventario (producto_id, cantidad, tipo_movimiento, fecha_movimiento)
SELECT (SELECT id FROM producto WHERE nombre = 'Arroz 1kg'), 100, 'Entrada', CURRENT_DATE WHERE @seed_needed = 1;
INSERT INTO inventario (producto_id, cantidad, tipo_movimiento, fecha_movimiento)
SELECT (SELECT id FROM producto WHERE nombre = 'Coca-Cola 1.5L'), 60, 'Entrada', CURRENT_DATE WHERE @seed_needed = 1;
INSERT INTO inventario (producto_id, cantidad, tipo_movimiento, fecha_movimiento)
SELECT (SELECT id FROM producto WHERE nombre = 'Leche entera 1L'), 10, 'Salida', CURRENT_DATE WHERE @seed_needed = 1;

-- 7. VENTAS
INSERT INTO venta (usuario_id, fecha)
SELECT (SELECT id FROM usuario WHERE username = 'cliente'), CURRENT_DATE WHERE @seed_needed = 1;
INSERT INTO venta (usuario_id, fecha)
SELECT (SELECT id FROM usuario WHERE username = 'cliente'), CURRENT_DATE WHERE @seed_needed = 1;

-- 8. DETALLE DE VENTAS
INSERT INTO detalle_venta (venta_id, producto_id, cantidad, precio)
SELECT (SELECT MIN(id) FROM venta WHERE usuario_id = (SELECT id FROM usuario WHERE username = 'cliente')),
       (SELECT id FROM producto WHERE nombre = 'Arroz 1kg'), 2, 1290.0
WHERE @seed_needed = 1;
INSERT INTO detalle_venta (venta_id, producto_id, cantidad, precio)
SELECT (SELECT MIN(id) FROM venta WHERE usuario_id = (SELECT id FROM usuario WHERE username = 'cliente')),
       (SELECT id FROM producto WHERE nombre = 'Coca-Cola 1.5L'), 3, 1590.0
WHERE @seed_needed = 1;