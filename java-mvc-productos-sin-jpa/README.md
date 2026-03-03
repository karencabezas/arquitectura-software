# 🛍️ Spring Boot MVC — Gestión de Productos y Marcas (sin JPA)

Aplicación web CRUD desarrollada con **Spring Boot 3.2**, **Spring MVC**, **JdbcTemplate**, **Thymeleaf** y **PostgreSQL**. A diferencia de los enfoques con JPA/Hibernate, este proyecto accede a la base de datos directamente mediante **SQL nativo** usando `JdbcTemplate`.

---

## 🧰 Tecnologías utilizadas

- Java 17
- Spring Boot 3.2.3
- Spring MVC + Spring JDBC (`JdbcTemplate`)
- Thymeleaf (motor de plantillas)
- PostgreSQL
- Bootstrap 5.3
- Maven

---

## 📁 Estructura del proyecto

```
src/main/java/com/demo/productmanagement/
├── controller/
│   ├── ProductController.java
│   └── MarcaController.java
├── model/
│   ├── Product.java
│   └── Marca.java
├── repository/
│   ├── ProductRepository.java
│   └── MarcaRepository.java
├── service/
│   ├── ProductService.java
│   ├── ProductServiceImpl.java
│   ├── MarcaService.java
│   └── MarcaServiceImpl.java
└── MvcCrudProductosSinJpaApplication.java

src/main/resources/
├── templates/
│   ├── fragments/
│   │   └── layout.html
│   ├── index.html
│   ├── products/
│   │   ├── list.html
│   │   ├── form.html
│   │   └── view.html
│   └── marcas/
│       ├── list.html
│       ├── form.html
│       └── view.html
├── static/css/
│   └── main.css
├── application.properties
└── schema.sql
```

---

## ⚙️ Configuración de la base de datos

1. Asegúrate de tener **PostgreSQL** instalado y corriendo.

2. Crea la base de datos:

```sql
CREATE DATABASE product_db01;
```

3. Las tablas se crean **automáticamente** al iniciar la aplicación gracias a la propiedad:

```properties
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:schema.sql
```

> El archivo `schema.sql` contiene la definición de las tablas `productos` y `marcas`, además de datos de prueba.

4. Verifica que las credenciales en `application.properties` coincidan con tu entorno:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/product_db01
spring.datasource.username=postgres
spring.datasource.password=admin
```

---

## 🚀 Cómo ejecutar el proyecto

### Opción 1 — Maven (desarrollo)

```bash
# Compilar el proyecto
mvn clean package

# Ejecutar con Spring Boot
mvn spring-boot:run
```

### Opción 2 — JAR compilado

```bash
mvn clean package
java -jar target/product-management-0.0.1-SNAPSHOT.jar
```

La aplicación estará disponible en: [http://localhost:3000](http://localhost:3000)

---

## 🌐 Endpoints disponibles

| Ruta | Descripción |
|------|-------------|
| `GET /` | Página principal |
| `GET /products` | Listado de productos |
| `GET /products/new` | Formulario para crear producto |
| `POST /products` | Guardar nuevo producto |
| `GET /products/edit/{id}` | Formulario para editar producto |
| `POST /products/update` | Actualizar producto existente |
| `GET /products/view/{id}` | Ver detalle de un producto |
| `GET /products/delete/{id}` | Eliminar producto |
| `GET /marcas` | Listado de marcas |
| `GET /marcas/new` | Formulario para crear marca |
| `POST /marcas` | Guardar nueva marca |
| `GET /marcas/edit/{id}` | Formulario para editar marca |
| `POST /marcas/update` | Actualizar marca existente |
| `GET /marcas/view/{id}` | Ver detalle de una marca |
| `GET /marcas/delete/{id}` | Eliminar marca |

---

## 📌 Notas

- Este proyecto **no usa JPA ni Hibernate**. Toda la comunicación con la base de datos se realiza con `JdbcTemplate` y SQL nativo, lo que lo hace ideal para aprender el acceso a datos sin abstracción de ORM.
- El esquema se reinicia en cada arranque (`schema.sql` incluye `DROP TABLE IF EXISTS`). Para persistir datos entre reinicios, cambia `spring.sql.init.mode=always` a `never`.
- El servidor corre en el puerto **3000** (configurable en `application.properties`).