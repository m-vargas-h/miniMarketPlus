-- DATOS INICIALES - MINIMARKET

-- 1. ROLES
-- Se definen 3 roles segun los tipos de usuario del sistema
INSERT INTO rol (nombre) VALUES ('ROLE_ADMIN');
INSERT INTO rol (nombre) VALUES ('ROLE_EMPLEADO');
INSERT INTO rol (nombre) VALUES ('ROLE_CLIENTE');

-- 2. USUARIOS

INSERT INTO usuario (username, password) VALUES (
    'admin',
    '$2a$10$RRVPp2ecFm0PM8I7Ssq2deNhzWJ8x0RxGvSmXcwAWuMv/USgGWdIq'
);
INSERT INTO usuario (username, password) VALUES (
    'empleado',
    '$2a$10$Gu9R.L/tQm7jjRn3UjcyhOfmdy4SGFH4jMoIifPLypCRil4uAiWp2'
);
INSERT INTO usuario (username, password) VALUES (
    'cliente',
    '$2a$10$rKRhKD5.COEvLwwFOBs2Wusfn5GwjZMhVAAREXUsoXzIs1thdoAcu'
);

-- 3. ASIGNACION DE ROLES A USUARIOS
INSERT INTO usuario_roles (usuario_id, rol_id) VALUES (1, 1); -- admin    -> ROLE_ADMIN
INSERT INTO usuario_roles (usuario_id, rol_id) VALUES (2, 2); -- empleado -> ROLE_EMPLEADO
INSERT INTO usuario_roles (usuario_id, rol_id) VALUES (3, 3); -- cliente  -> ROLE_CLIENTE