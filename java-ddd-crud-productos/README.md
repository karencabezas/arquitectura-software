# Demo DDD CRUD — Productos y Clientes

---

## Requisitos

- Java 17
- Maven
- PostgreSQL

---

## 1. Crear la base de datos

Abrir pgAdmin o psql y ejecutar:

```sql
CREATE DATABASE postgres;
```

> Si la base `postgres` ya existe, omitir este paso.

---

## 2. Configurar la conexión

Editar el archivo `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=admin
```

Cambiar `username` y `password` según tu instalación de PostgreSQL.

---

## 3. Ejecutar el proyecto

```bash
mvn spring-boot:run
```

Al iniciar, **se crean las tablas automáticamente**.

La API queda disponible en: `http://localhost:8000`

---

## 4. Probar con Postman

Importar el archivo `Cliente.postman_collection.json` que se encuentra en la raíz del proyecto:

1. Abrir Postman
2. Clic en **Import**
3. Seleccionar el archivo `Cliente.postman_collection.json`
4. Ejecutar los requests incluidos

---

## Endpoints disponibles

### Clientes — `/api/clientes`

| Método | URL | Descripción |
|--------|-----|-------------|
| POST | `/api/clientes` | Crear cliente |
| GET | `/api/clientes` | Listar todos |
| GET | `/api/clientes/{id}` | Buscar por ID |
| GET | `/api/clientes/numeroDocumento/{id}` | Buscar por numeroDocumento |
| PUT | `/api/clientes/{id}` | Actualizar cliente |
| PATCH | `/api/clientes/{id}/estado` | Activar o desactivar cliente |
| DELETE | `/api/clientes/{id}` | Eliminar cliente |

### Productos — `/api/productos`

| Método | URL | Descripción |
|--------|-----|-------------|
| POST | `/api/productos` | Crear producto |
| GET | `/api/productos` | Listar todos |
| GET | `/api/productos/{id}` | Buscar por ID |
| PUT | `/api/productos/{id}` | Actualizar producto |
| DELETE | `/api/productos/{id}` | Eliminar producto |

