-- ============================================================
-- Script de inicialización: Auth + MFA + RBAC + ABAC
-- Base de datos: authdb
-- ============================================================

-- ===== TABLAS =====

CREATE TABLE IF NOT EXISTS usuarios (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    totp_secret   VARCHAR(255),
    mfa_enabled   BOOLEAN NOT NULL DEFAULT FALSE,
    activo        BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS roles (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(100) UNIQUE NOT NULL,
    descripcion VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS permisos (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(100) UNIQUE NOT NULL,
    descripcion VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS usuario_roles (
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    rol_id     BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (usuario_id, rol_id)
);

CREATE TABLE IF NOT EXISTS rol_permisos (
    rol_id     BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permiso_id BIGINT NOT NULL REFERENCES permisos(id) ON DELETE CASCADE,
    PRIMARY KEY (rol_id, permiso_id)
);

CREATE TABLE IF NOT EXISTS productos (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(255) NOT NULL,
    descripcion TEXT,
    precio      NUMERIC(12, 2) NOT NULL,
    categoria   VARCHAR(100),
    owner_id    BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    activo      BOOLEAN NOT NULL DEFAULT TRUE
);

-- ===== ÍNDICES =====

CREATE INDEX IF NOT EXISTS idx_usuarios_email     ON usuarios(email);
CREATE INDEX IF NOT EXISTS idx_productos_owner    ON productos(owner_id);
CREATE INDEX IF NOT EXISTS idx_productos_activo   ON productos(activo);

-- ===== DATOS INICIALES =====

-- Permisos para ABAC sobre productos
INSERT INTO permisos (nombre, descripcion) VALUES
    ('PRODUCT_SELECT', 'Ver productos'),
    ('PRODUCT_INSERT', 'Crear productos'),
    ('PRODUCT_UPDATE', 'Actualizar productos'),
    ('PRODUCT_DELETE', 'Eliminar productos')
ON CONFLICT (nombre) DO NOTHING;

-- Roles
INSERT INTO roles (nombre, descripcion) VALUES
    ('ADMIN', 'Administrador con acceso total'),
    ('USER',  'Usuario estándar con acceso básico')
ON CONFLICT (nombre) DO NOTHING;

-- Asignar TODOS los permisos al rol ADMIN
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.nombre = 'ADMIN'
ON CONFLICT DO NOTHING;

-- Asignar solo SELECT e INSERT al rol USER
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
JOIN permisos p ON p.nombre IN ('PRODUCT_SELECT', 'PRODUCT_INSERT')
WHERE r.nombre = 'USER'
ON CONFLICT DO NOTHING;

-- Usuario administrador inicial
-- Password: Admin1234! (BCrypt hash)
INSERT INTO usuarios (email, password_hash, mfa_enabled, activo)
VALUES ('admin@authapp.com', '$2a$10$TcodmDta.bBys/Xw4PWgiesWVDmP331tqleanRCGS8ryyLrmjtlxq', FALSE, TRUE)
ON CONFLICT (email) DO NOTHING;

-- Usuario de prueba estándar
-- Password: User1234!
INSERT INTO usuarios (email, password_hash, mfa_enabled, activo)
VALUES ('user@authapp.com', '$2a$10$V2RBC7jTYlmcsTCWnLll5eKZh9NzroZGxQUvfwBKrGnwZLeliUTfC', FALSE, TRUE)
ON CONFLICT (email) DO NOTHING;

-- Asignar rol ADMIN al usuario admin
INSERT INTO usuario_roles (usuario_id, rol_id)
SELECT u.id, r.id
FROM usuarios u
JOIN roles r ON r.nombre = 'ADMIN'
WHERE u.email = 'admin@authapp.com'
ON CONFLICT DO NOTHING;

-- Asignar rol USER al usuario de prueba
INSERT INTO usuario_roles (usuario_id, rol_id)
SELECT u.id, r.id
FROM usuarios u
JOIN roles r ON r.nombre = 'USER'
WHERE u.email = 'user@authapp.com'
ON CONFLICT DO NOTHING;

-- Productos de ejemplo
INSERT INTO productos (nombre, descripcion, precio, categoria, owner_id, activo)
SELECT 'Laptop Pro 15', 'Laptop de alto rendimiento', 2499.99, 'Electrónica', u.id, TRUE
FROM usuarios u WHERE u.email = 'admin@authapp.com'
ON CONFLICT DO NOTHING;

INSERT INTO productos (nombre, descripcion, precio, categoria, owner_id, activo)
SELECT 'Monitor 4K', 'Monitor UltraWide 34 pulgadas', 899.99, 'Electrónica', u.id, TRUE
FROM usuarios u WHERE u.email = 'user@authapp.com'
ON CONFLICT DO NOTHING;

INSERT INTO productos (nombre, descripcion, precio, categoria, owner_id, activo)
SELECT 'Teclado Mecánico', 'Teclado mecánico RGB', 149.99, 'Periféricos', u.id, TRUE
FROM usuarios u WHERE u.email = 'admin@authapp.com'
ON CONFLICT DO NOTHING;
