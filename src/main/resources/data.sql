-- 1. ROLES
INSERT INTO rol (nombre) VALUES ('ROLE_ADMIN');
INSERT INTO rol (nombre) VALUES ('ROLE_EMPLEADO');
INSERT INTO rol (nombre) VALUES ('ROLE_CLIENTE');

-- 2. USUARIOS
INSERT INTO usuario (username, password) VALUES (
    'admin',
    '$2a$10$EeVxc3zewmmsgWpyAFizMuoQoLd6wLlqRjz8QvjzD3.2gl9vl21w6'
);
INSERT INTO usuario (username, password) VALUES (
    'empleado',
    '$2a$10$w8IaAghVbCBP7QVjt37Mg.nj99qMDebPS5BRWZRMYno9h//9OXgwu'
);
INSERT INTO usuario (username, password) VALUES (
    'cliente',
    '$2a$10$ZOKshDyLuehAyjf4nrCCtuIp6E0tyohTzAF4cy3ZORhpDino5gMTa'
);

-- 3. ASIGNACION DE ROLES
INSERT INTO usuario_roles (usuario_id, rol_id) 
    VALUES ((SELECT id FROM usuario WHERE username = 'admin'), 
            (SELECT id FROM rol WHERE nombre = 'ROLE_ADMIN'));

INSERT INTO usuario_roles (usuario_id, rol_id) 
    VALUES ((SELECT id FROM usuario WHERE username = 'empleado'), 
            (SELECT id FROM rol WHERE nombre = 'ROLE_EMPLEADO'));

INSERT INTO usuario_roles (usuario_id, rol_id) 
    VALUES ((SELECT id FROM usuario WHERE username = 'cliente'), 
            (SELECT id FROM rol WHERE nombre = 'ROLE_CLIENTE'));

-- 4. CATEGORIAS
INSERT INTO categoria (nombre) VALUES ('Abarrotes');
INSERT INTO categoria (nombre) VALUES ('Bebidas');
INSERT INTO categoria (nombre) VALUES ('Lácteos');
INSERT INTO categoria (nombre) VALUES ('Aseo y limpieza');