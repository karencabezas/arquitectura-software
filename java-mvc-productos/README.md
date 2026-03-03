# 🛍️ Spring Boot MVC — Gestión de Productos y Marcas

Aplicación web CRUD desarrollada con **Spring Boot 2.7**, **Spring MVC**, **JPA/Hibernate**, **Thymeleaf** y **PostgreSQL**. Permite gestionar un catálogo de productos y marcas desde el navegador.

---

## 🧰 Tecnologías utilizadas

- Java 11
- Spring Boot 2.7.9
- Spring MVC + Spring Data JPA
- Hibernate (ORM)
- Thymeleaf (motor de plantillas)
- PostgreSQL
- Bootstrap 5.1.3
- Maven

---

## 📁 Estructura del proyecto

```
src/main/java/com/demo/productos/
├── controller/
│   ├── ProductoController.java
│   └── MarcaController.java
├── model/
│   ├── Producto.java
│   └── Marca.java
├── repository/
│   ├── ProductoRepository.java
│   └── MarcaRepository.java
├── service/
│   ├── ProductoService.java
│   ├── ProductoServiceImpl.java
│   ├── MarcaService.java
│   └── MarcaServiceImpl.java
└── MvcProductosApplication.java

src/main/resources/
├── templates/
│   ├── listar-productos.html
│   ├── crear-producto.html
│   ├── editar-producto.html
│   ├── listar-marcas.html
│   ├── crear-marca.html
│   └── editar-marca.html
├── application.properties
└── db.sql
```

---

## ⚙️ Configuración de la base de datos

1. Asegúrate de tener **PostgreSQL** instalado y corriendo.

2. Crea la base de datos:

```sql
CREATE DATABASE productos_db01;
```

3. Ejecuta el script `src/main/resources/db.sql` para crear las tablas e insertar datos de prueba:

```bash
psql -U postgres -d productos_db01 -f src/main/resources/db.sql
```

4. Verifica que las credenciales en `application.properties` coincidan con tu entorno:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/productos_db01
spring.datasource.username=postgres
spring.datasource.password=admin
```

---

## 🚀 Cómo ejecutar el proyecto

```bash
# Compilar el proyecto
mvn clean install

# Ejecutar con Spring Boot
mvn spring-boot:run
```

La aplicación estará disponible en: [http://localhost:9000](http://localhost:9000)

---

## 🌐 Endpoints disponibles

| Ruta | Descripción |
|------|-------------|
| `GET /productos` | Listado de productos |
| `GET /productos/nuevo` | Formulario para crear producto |
| `POST /productos/guardar` | Guardar nuevo producto |
| `GET /productos/editar/{id}` | Formulario para editar producto |
| `POST /productos/actualizar/{id}` | Actualizar producto existente |
| `GET /productos/eliminar/{id}` | Eliminar producto |
| `GET /productos/buscar?nombre=` | Buscar productos por nombre |
| `GET /marcas` | Listado de marcas |
| `GET /marcas/nueva` | Formulario para crear marca |
| `POST /marcas/guardar` | Guardar nueva marca |
| `GET /marcas/editar/{id}` | Formulario para editar marca |
| `POST /marcas/actualizar/{id}` | Actualizar marca existente |
| `GET /marcas/eliminar/{id}` | Eliminar marca |
| `GET /marcas/buscar?nombre=` | Buscar marcas por nombre |

---

## 📌 Notas

- La propiedad `spring.jpa.hibernate.ddl-auto=update` permite que Hibernate cree o actualice las tablas automáticamente al iniciar la aplicación.
- `spring.thymeleaf.cache=false` está habilitado para facilitar el desarrollo en caliente con DevTools.
- El servidor corre en el puerto **9000** (configurable en `application.properties`).
