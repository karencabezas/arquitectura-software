# AuthApp — Auth + MFA (TOTP) + RBAC + ABAC

Aplicación desarrollada para el curso de **Arquitectura de Software**, implementando:

- **Autenticación en dos fases**: email/password + TOTP (Google Authenticator)
- **RBAC**: Control de acceso basado en roles (Admin, User)
- **ABAC**: Control de acceso basado en atributos sobre productos
- **Clean Architecture** con separación estricta en 3 capas: `domain`, `application`, `infrastructure`

---

## Stack Tecnológico

| Capa         | Tecnología                                        |
|--------------|---------------------------------------------------|
| Backend      | Java 17 + Spring Boot 3.2                         |
| Seguridad    | Spring Security + JJWT + dev.samstevens.totp      |
| Base de datos| PostgreSQL 16                                     |
| Frontend     | Angular 18 (standalone components + signals)      |
| Contenedores | Docker + Docker Compose                           |

---

## Arquitectura del Backend — Clean Architecture (3 capas)

La arquitectura sigue el principio fundamental: **las dependencias apuntan siempre hacia adentro**. El dominio no conoce a nadie. La infraestructura conoce a todos.

```
┌─────────────────────────────────────────────────────────────┐
│                     INFRASTRUCTURE                          │
│  (Spring, JPA, JWT, TOTP, Controllers, Adapters)            │
│                                                             │
│   ┌─────────────────────────────────────────────────────┐  │
│   │                   APPLICATION                       │  │
│   │   (Casos de uso: AuthService, RbacService,          │  │
│   │    ProductoService)                                 │  │
│   │                                                     │  │
│   │   ┌───────────────────────────────────────────┐    │  │
│   │   │               DOMAIN                      │    │  │
│   │   │  model: Usuario, Rol, Permiso, Producto   │    │  │
│   │   │  port:  UsuarioRepositoryPort, ...        │    │  │
│   │   │                                           │    │  │
│   │   │  ← Sin dependencias externas              │    │  │
│   │   └───────────────────────────────────────────┘    │  │
│   └─────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Regla de dependencias

```
domain         →  no depende de nadie
application    →  depende solo de domain
infrastructure →  depende de domain + application + frameworks externos
```

---

## Estructura de Paquetes Completa

```
com.authapp/
│
├── domain/                              ← CAPA 1: NÚCLEO. Sin Spring, sin JPA, sin nada externo.
│   ├── model/
│   │   ├── Usuario.java                 ← Entidad pura con reglas de negocio:
│   │   │                                    estaActivo(), tieneMfaConfigurado(),
│   │   │                                    obtenerNombresRoles(), obtenerNombresPermisos()
│   │   ├── Rol.java                     ← tienePermiso(), obtenerNombresPermisos()
│   │   ├── Permiso.java
│   │   └── Producto.java                ← perteneceA(usuarioId) — regla ABAC en dominio
│   └── port/                            ← Interfaces que el dominio EXIGE, infraestructura implementa
│       ├── UsuarioRepositoryPort.java
│       ├── RolRepositoryPort.java
│       ├── PermisoRepositoryPort.java
│       └── ProductoRepositoryPort.java
│
├── application/                         ← CAPA 2: CASOS DE USO. Orquesta dominio y ports.
│   │                                       Depende de domain. NO conoce JPA ni Spring Data.
│   ├── auth/
│   │   └── AuthService.java             ← login() fase 1, validateMfa() fase 2,
│   │                                        setupMfa(), confirmMfaSetup(), register()
│   ├── rbac/
│   │   └── RbacService.java             ← CRUD roles/permisos, asignación usuario↔rol
│   └── product/
│       └── ProductoService.java         ← CRUD productos con validación ABAC
│
└── infrastructure/                      ← CAPA 3: DETALLES TÉCNICOS. Adapta el exterior al interior.
    ├── persistence/
    │   ├── entity/                      ← Entidades JPA (@Entity, @Table, @Column...)
    │   │   ├── UsuarioEntity.java           Solo viven aquí, el dominio no las conoce
    │   │   ├── RolEntity.java
    │   │   ├── PermisoEntity.java
    │   │   └── ProductoEntity.java
    │   ├── repository/                  ← Interfaces Spring Data JPA (solo usadas por adapters)
    │   │   ├── UsuarioRepository.java
    │   │   ├── RolRepository.java
    │   │   ├── PermisoRepository.java
    │   │   └── ProductoRepository.java
    │   └── adapter/                     ← Implementan los ports del dominio usando JPA
    │       ├── DomainMapper.java        ← Traduce: Entity → Domain model y Domain model → Entity
    │       ├── UsuarioRepositoryAdapter.java  ← implements UsuarioRepositoryPort
    │       ├── RolRepositoryAdapter.java      ← implements RolRepositoryPort
    │       └── OtrosAdapters.java             ← Permiso + Producto adapters
    ├── security/
    │   ├── JwtService.java              ← Genera/valida pre-auth token (2 min) y full token (24h)
    │   ├── TotpService.java             ← Genera secret, QR URI y verifica códigos TOTP
    │   ├── JwtAuthFilter.java           ← Intercepta cada request y construye AppUserPrincipal
    │   ├── AppUserPrincipal.java        ← Principal con id, roles y permisos extraídos del JWT
    │   └── SecurityConfig.java          ← Spring Security: rutas públicas, CORS, STATELESS
    └── web/
        ├── controller/
        │   ├── AuthController.java      ← POST /api/auth/*
        │   ├── AdminController.java     ← /api/admin/* protegido con @PreAuthorize("hasRole('ADMIN')")
        │   ├── ProductoController.java  ← /api/productos/* — delega lógica ABAC al service
        │   └── GlobalExceptionHandler.java ← Maneja AuthException, AccessDeniedException, etc.
        └── dto/
            ├── AuthDtos.java
            └── ProductoDtos.java
```

---

## Patrón Ports & Adapters explicado

Este es el patrón clave que desacopla las capas sin romper el funcionamiento:

```
┌──────────────────┐     usa      ┌─────────────────────────┐
│  AuthService     │ ──────────►  │  UsuarioRepositoryPort  │  ← interface en domain/port
│  (application)   │              └─────────────────────────┘
└──────────────────┘                          ▲
                                              │ implementa
                               ┌──────────────────────────────────┐
                               │  UsuarioRepositoryAdapter        │  ← en infrastructure/adapter
                               │    usa DomainMapper              │
                               │    usa UsuarioRepository (JPA)   │
                               └──────────────────────────────────┘
```

`AuthService` nunca importa `UsuarioRepository` ni `UsuarioEntity`.
Solo conoce `UsuarioRepositoryPort` y `Usuario` (modelo de dominio puro).
Si mañana cambias PostgreSQL por MongoDB, solo reemplazas el adapter — el resto no se toca.

---

## Modelo de Base de Datos

```sql
usuarios        -- id | email | password_hash | totp_secret | mfa_enabled | activo
roles           -- id | nombre | descripcion
permisos        -- id | nombre | descripcion
usuario_roles   -- usuario_id | rol_id          (RBAC: un usuario tiene varios roles)
rol_permisos    -- rol_id | permiso_id          (RBAC: un rol agrupa varios permisos)
productos       -- id | nombre | descripcion | precio | categoria | owner_id | activo
```

**Permisos ABAC predefinidos:**

| Permiso          | Descripción          |
|------------------|----------------------|
| PRODUCT_SELECT   | Ver productos        |
| PRODUCT_INSERT   | Crear productos      |
| PRODUCT_UPDATE   | Actualizar productos |
| PRODUCT_DELETE   | Eliminar productos   |

**Roles predefinidos:**

| Rol   | Permisos asignados                               |
|-------|--------------------------------------------------|
| ADMIN | SELECT + INSERT + UPDATE + DELETE (acceso total) |
| USER  | SELECT + INSERT únicamente                       |

---

## Flujo de Autenticación — Paso a Paso

### Primera vez (sin MFA configurado)

```
1. POST /api/auth/login
   Body: { "email": "...", "password": "..." }
   ← { "preAuthToken": "eyJ...", "mfaRequired": false }

   preAuthToken: JWT de 2 minutos con claim "mfa_pending: true".
   No sirve para acceder a recursos protegidos.

2. POST /api/auth/mfa/setup
   Header: Authorization: Bearer <preAuthToken>
   ← { "qrImageUri": "data:image/png;base64,...", "secret": "JBSWY3..." }
   El usuario escanea el QR con Google Authenticator.

3. POST /api/auth/mfa/setup/confirm
   Header: Authorization: Bearer <preAuthToken>
   Body: { "totpCode": "123456" }
   ← { "token": "<JWT_FINAL>", "roles": [...], "permissions": [...] }
   MFA activado. En adelante se pedirá código en cada login.
```

### Login normal (con MFA ya activo)

```
1. POST /api/auth/login
   Body: { "email": "...", "password": "..." }
   ← { "preAuthToken": "eyJ...", "mfaRequired": true }

2. POST /api/auth/mfa/validate
   Body: { "preAuthToken": "eyJ...", "totpCode": "123456" }
   ← { "token": "<JWT_FINAL>", "roles": ["ADMIN"], "permissions": [...] }

   Usar en cada request: Authorization: Bearer <JWT_FINAL>
```

### Estructura del JWT final

```json
{
  "sub": "admin@authapp.com",
  "type": "FULL_AUTH",
  "userId": 1,
  "roles": ["ADMIN"],
  "permissions": ["PRODUCT_SELECT", "PRODUCT_INSERT", "PRODUCT_UPDATE", "PRODUCT_DELETE"],
  "iat": 1704067200,
  "exp": 1704153600
}
```

---

## RBAC vs ABAC — Diferencia clave

| Aspecto | RBAC | ABAC |
|---|---|---|
| Decisión basada en | Rol del usuario | Rol + atributos del usuario + atributos del recurso |
| Ejemplo | ¿Es ADMIN? → accede al panel | ¿Es dueño del producto Y tiene PRODUCT_DELETE? → puede borrar |
| Implementación | `@PreAuthorize("hasRole('ADMIN')")` en controller | Lógica explícita en `application/` service |
| Flexibilidad | Baja (binario: tiene el rol o no) | Alta (múltiples condiciones combinadas) |

### RBAC en el código

```java
// AdminController.java — protege la ruta entera por rol
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/roles")
public ResponseEntity<?> listarRoles() { ... }
```

### ABAC en el código

```java
// ProductoService.java — evalúa atributos del usuario Y del recurso
public void eliminar(Long id, AppUserPrincipal principal) {
    Producto producto = productoPort.findById(id)...;

    // Regla ABAC 1: dueño o ADMIN — usa método del modelo de DOMINIO
    if (!principal.isAdmin() && !producto.perteneceA(principal.getId()))
        throw new AccessDeniedException("Solo el dueño puede eliminar este producto");

    // Regla ABAC 2: además necesita el permiso explícito
    if (!principal.hasPermission("PRODUCT_DELETE") && !principal.isAdmin())
        throw new AccessDeniedException("No tienes permiso para eliminar productos");

    producto.setActivo(false);
    productoPort.save(producto);      // ← usa el port, nunca JPA directamente
}
```

---

## Endpoints de la API

### Autenticación (público)

| Método | Ruta                        | Descripción                          |
|--------|-----------------------------|--------------------------------------|
| POST   | /api/auth/register          | Registrar nuevo usuario              |
| POST   | /api/auth/login             | Fase 1: validar email + password     |
| POST   | /api/auth/mfa/validate      | Fase 2: validar código TOTP          |
| POST   | /api/auth/mfa/setup         | Generar QR para Google Authenticator |
| POST   | /api/auth/mfa/setup/confirm | Confirmar y activar MFA              |
| GET    | /api/auth/me                | Datos del usuario autenticado        |

### Admin — RBAC (requiere rol ADMIN)

| Método | Ruta                                   | Descripción               |
|--------|----------------------------------------|---------------------------|
| GET    | /api/admin/roles                       | Listar roles              |
| POST   | /api/admin/roles                       | Crear rol                 |
| PUT    | /api/admin/roles/{id}                  | Actualizar rol            |
| DELETE | /api/admin/roles/{id}                  | Eliminar rol              |
| GET    | /api/admin/permisos                    | Listar permisos           |
| POST   | /api/admin/roles/{rolId}/permisos      | Asignar permisos a rol    |
| DELETE | /api/admin/roles/{rolId}/permisos/{id} | Quitar permiso de rol     |
| GET    | /api/admin/usuarios                    | Listar usuarios           |
| POST   | /api/admin/usuarios/{uid}/roles/{rid}  | Asignar rol a usuario     |
| DELETE | /api/admin/usuarios/{uid}/roles/{rid}  | Quitar rol de usuario     |

### Productos — ABAC (requiere autenticación)

| Método | Ruta                | Regla ABAC                               |
|--------|---------------------|------------------------------------------|
| GET    | /api/productos      | Cualquier usuario autenticado            |
| GET    | /api/productos/{id} | Cualquier usuario autenticado            |
| POST   | /api/productos      | PRODUCT_INSERT o ADMIN                   |
| PUT    | /api/productos/{id} | (Dueño + PRODUCT_UPDATE) o ADMIN         |
| DELETE | /api/productos/{id} | (Dueño + PRODUCT_DELETE) o ADMIN         |

---

## Usuarios de Prueba

| Email               | Password   | Rol   | MFA            |
|---------------------|------------|-------|----------------|
| admin@authapp.com   | Admin1234! | ADMIN | No configurado |
| user@authapp.com    | User1234!  | USER  | No configurado |

> Al hacer login por primera vez el sistema redirige al flujo de setup MFA automáticamente.

---

## Cómo Levantar el Proyecto

### Opción A — Todo en Docker (recomendado)

**Requisitos:** Docker Desktop instalado y corriendo.

```bash
cd proyecto
docker-compose up --build

# Primera vez: ~5 minutos (Maven + npm descargan dependencias)
# Frontend  → http://localhost:4200
# Backend   → http://localhost:8080

# Detener:
docker-compose down

# Detener y borrar datos de BD:
docker-compose down -v
```

### Opción B — Solo BD en Docker, código local

**Requisitos:** Java 17, Maven 3.9+, Node 20+, Angular CLI 18, Docker Desktop.

```bash
# Terminal 1 — PostgreSQL
docker-compose -f docker-compose.db-only.yml up -d

# Terminal 2 — Backend
cd backend && mvn spring-boot:run

# Terminal 3 — Frontend
cd frontend && npm install && ng serve
```

### Opción C — Sin Docker

**Requisitos:** Todo lo anterior más PostgreSQL 16 instalado localmente.

```sql
CREATE DATABASE authdb;
\i database/init.sql
```

```bash
export DB_URL=jdbc:postgresql://localhost:5432/authdb
export DB_USER=postgres
export DB_PASSWORD=tu_password

cd backend && mvn spring-boot:run
cd frontend && npm install && ng serve
```

---

## Verificación Rápida con curl

```bash
# 1. Login
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@authapp.com","password":"Admin1234!"}' | jq

# 2. Guardar el preAuthToken
PRE_TOKEN="<preAuthToken del paso anterior>"

# 3. Obtener QR para Google Authenticator
curl -s -X POST http://localhost:8080/api/auth/mfa/setup \
  -H "Authorization: Bearer $PRE_TOKEN" | jq

# 4. Confirmar MFA con el primer código de la app
curl -s -X POST http://localhost:8080/api/auth/mfa/setup/confirm \
  -H "Authorization: Bearer $PRE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"totpCode":"123456"}' | jq

# 5. Usar el JWT final
JWT="<token del paso anterior>"

curl -s http://localhost:8080/api/productos -H "Authorization: Bearer $JWT" | jq
curl -s http://localhost:8080/api/admin/roles -H "Authorization: Bearer $JWT" | jq
```

---

## Variables de Entorno

| Variable     | Default                                 | Descripción                  |
|--------------|-----------------------------------------|------------------------------|
| DB_URL       | jdbc:postgresql://localhost:5432/authdb | Conexión a PostgreSQL        |
| DB_USER      | postgres                                | Usuario PostgreSQL            |
| DB_PASSWORD  | postgres                                | Contraseña PostgreSQL         |
| JWT_SECRET   | (base64 256 bits incluida)              | Secreto para firmar JWTs     |
| CORS_ORIGINS | http://localhost:4200                   | Orígenes CORS permitidos     |

> ⚠️ En producción cambia `JWT_SECRET` por una cadena aleatoria segura de al menos 32 caracteres en Base64.

---

## Diagrama de Flujo Completo

```
Usuario              Angular              Spring Boot                 PostgreSQL
  │                    │                       │                           │
  │── email+pass ─────►│                       │                           │
  │                    │── POST /auth/login ──►│                           │
  │                    │                       │── UsuarioRepositoryPort  │
  │                    │                       │   (adapter usa JPA) ─────►│
  │                    │                       │◄── UsuarioEntity ─────────│
  │                    │                       │   DomainMapper            │
  │                    │                       │   → Usuario (dominio)     │
  │                    │                       │   BCrypt.matches()        │
  │                    │◄── preAuthToken ───────│                           │
  │◄── "Escanee QR" ───│                       │                           │
  │                    │                       │                           │
  │── escanea QR ─────►[Google Authenticator]  │                           │
  │── código TOTP ─────►│                      │                           │
  │                    │── POST /auth/mfa/validate ──►│                    │
  │                    │                       │   TOTP.verify()           │
  │                    │◄── JWT final (24h) ────│                           │
  │                    │   {roles,permissions}  │                           │
  │                    │                       │                           │
  │── click Eliminar ──►│                      │                           │
  │   (si canDelete()) │── DELETE /productos/{id} ──►│                    │
  │                    │   Bearer JWT          │   JwtAuthFilter           │
  │                    │                       │   AppUserPrincipal        │
  │                    │                       │   ProductoService         │
  │                    │                       │   producto.perteneceA()   │
  │                    │                       │   hasPermission()         │
  │                    │                       │── save() via port ───────►│
  │                    │◄── 200 OK ─────────────│                           │
  │◄── UI actualizada ──│                      │                           │
```

---

## Análisis de Calidad — SonarQube

El backend (`auth-backend`) fue analizado con **SonarQube Community Edition** sobre el JAR compilado con Maven.

### Cómo ejecutar el análisis

**Requisitos:** SonarQube levantado en `http://localhost:9000`.

La configuración de conexión (URL y token) está declarada en `~/.m2/settings.xml` bajo el perfil `sonar`, por lo que no es necesario pasar parámetros extra en la línea de comandos.

```bash
# 1. Levantar SonarQube
cd sonarqube-26.4.0.121862\bin\windows-x86-64
./StartSonar.bat

# 2. Compilar, ejecutar pruebas con cobertura (JaCoCo) y enviar el análisis
cd backend
mvn clean verify sonar:sonar

# 3. Ver el dashboard
# http://localhost:9000 → proyecto "auth-backend"
```

> El perfil `sonar` en `settings.xml` está configurado con `activeByDefault=true`,  
> por lo que se activa automáticamente en cada ejecución de Maven sin flags adicionales.

### Resultados obtenidos (Overall Code — v1.0.0 · 1.5k líneas)

| Dimensión            | Resultado         | Rating |
|----------------------|-------------------|--------|
| Security             | 0 open issues     | **A**  |
| Reliability          | 0 open issues     | **A**  |
| Maintainability      | 84 open issues    | **A**  |
| Security Hotspots    | 0                 | **A**  |
| Coverage             | **65.6 %**        | —      |
| Duplications         | 1.2 %             | —      |
| Lines to cover       | 583               | —      |
| Accepted issues      | 0                 | —      |

- **Seguridad y confiabilidad** obtienen calificación **A** sin issues abiertos, lo que confirma que no se detectaron vulnerabilidades ni bugs en el código analizado.
- La **cobertura de pruebas alcanza el 65.6 %** sobre 583 líneas medibles, cubriendo los casos de uso principales (autenticación, TOTP, RBAC y ABAC).
- Los 84 issues de mantenibilidad corresponden a _code smells_ menores (convenciones de nombres, complejidad cognitiva) que no afectan la funcionalidad ni la seguridad.
- La duplicación del **1.2 %** es despreciable para un proyecto de esta escala.

---

## Análisis de Seguridad Dinámica — OWASP ZAP

El frontend desplegado en Docker (`http://localhost:4200` vía Nginx) fue escaneado con **OWASP ZAP 2.17.0** en modo pasivo.

### Resumen de hallazgos

| Nivel de riesgo | Cantidad | Descripción                                              |
|-----------------|----------|----------------------------------------------------------|
| 🔴 Alto          | 0        | Sin hallazgos críticos                                   |
| 🟠 Medio         | 2        | Cabeceras de seguridad HTTP faltantes                    |
| 🟡 Bajo          | 2        | Filtración de versión de servidor y tipo de contenido    |
| 🔵 Informativo   | 2        | Aplicación web moderna detectada + tecnología Nginx      |

**No se encontraron vulnerabilidades de riesgo alto.** Todos los hallazgos corresponden a configuración de cabeceras HTTP en Nginx, sin impacto en la lógica de autenticación ni en los datos de los usuarios.

### Detalle de hallazgos

#### 🟠 Riesgo Medio

| Alerta | CWE | Descripción | Solución recomendada |
|--------|-----|-------------|----------------------|
| Content Security Policy (CSP) no configurada | CWE-693 | Nginx no envía la cabecera `Content-Security-Policy`, lo que facilita ataques XSS | Agregar `add_header Content-Security-Policy "default-src 'self'";` en `nginx.conf` |
| Falta cabecera Anti-Clickjacking | CWE-1021 | Ausencia de `X-Frame-Options` o `frame-ancestors` en CSP, lo que permite embeber la app en un iframe | Agregar `add_header X-Frame-Options "DENY";` en `nginx.conf` |

#### 🟡 Riesgo Bajo

| Alerta | CWE | Descripción | Solución recomendada |
|--------|-----|-------------|----------------------|
| Filtración de versión en cabecera `Server` | CWE-497 | Nginx expone su versión exacta (`nginx/1.x.x`) en las respuestas HTTP | Agregar `server_tokens off;` en `nginx.conf` |
| Falta cabecera `X-Content-Type-Options` | CWE-693 | Sin esta cabecera el navegador puede inferir el tipo MIME, facilitando ataques de sniffing | Agregar `add_header X-Content-Type-Options "nosniff";` en `nginx.conf` |

#### 🔵 Informativo (sin acción requerida)

| Alerta | Descripción |
|--------|-------------|
| Aplicación Web Moderna | ZAP detectó uso de JavaScript moderno (Angular SPA) |
| Tecnología Detectada — Nginx | ZAP identificó Nginx como servidor de archivos estáticos |

### Corrección recomendada en `nginx.conf`

Agregar el siguiente bloque dentro del `server { }` del contenedor del frontend:

```nginx
# Cabeceras de seguridad HTTP
add_header X-Frame-Options "DENY" always;
add_header X-Content-Type-Options "nosniff" always;
add_header Content-Security-Policy "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline';" always;
server_tokens off;
```

> Estas correcciones son de configuración de infraestructura y no requieren cambios en el código de la aplicación.
