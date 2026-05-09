-- ============================================================
-- fix-passwords.sql
-- Ejecutar este script si la BD ya estaba creada con hashes incorrectos.
-- Actualiza solo los password_hash de los usuarios de prueba.
-- ============================================================

UPDATE usuarios
SET password_hash = '$2a$10$TcodmDta.bBys/Xw4PWgiesWVDmP331tqleanRCGS8ryyLrmjtlxq'
WHERE email = 'admin@authapp.com';

UPDATE usuarios
SET password_hash = '$2a$10$V2RBC7jTYlmcsTCWnLll5eKZh9NzroZGxQUvfwBKrGnwZLeliUTfC'
WHERE email = 'user@authapp.com';

-- Verificar
SELECT email, LEFT(password_hash, 20) || '...' AS hash_preview, activo FROM usuarios;
