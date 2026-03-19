# 🚀 Guía de ejecución del proyecto

## 🧪 Ejecución en local (sin Docker)

mvn clean install  
mvn spring-boot:run  

### Endpoints:
http://localhost:8000/api/users  

---

## 🐳 Ejecución con Docker

### Levantar servicios:
docker-compose up --build  

O en segundo plano:
docker-compose up -d  

---

## 🔍 Verificar estado

http://localhost:9100/health  
http://localhost:9100/test  

---

## 🐰 RabbitMQ (Panel de administración)

http://localhost:15672  
Usuario: guest  
Contraseña: guest  

---

## 📌 Pruebas con CURL

### 👤 Crear usuario
curl -X POST http://localhost:9100/api/users \
-H "Content-Type: application/json" \
-d '{"name":"Juan Perez","email":"pepito@gmail.com","phone":"123456789"}'

curl -X POST http://localhost:9100/api/users \
-H "Content-Type: application/json" \
-d '{"name":"Juan Perez","email":"pepito@gmail.com","phone":"123456789"}'

---

### 📄 Obtener usuarios

curl -X GET http://localhost:9100/api/users  

curl -X GET http://localhost:9100/api/users/1  

---

## 📦 Productos

### Crear producto (envía email)
curl -X POST http://localhost:9100/api/products \
-H "Content-Type: application/json" \
-d '{"name":"laptop lenovo","code":"PROD-001","price":12.22,"stock":13,"email":"karen@gmail.com"}'

---

### Obtener productos

curl -X GET http://localhost:9100/api/products  

---

## 🛠️ Comandos útiles Docker

### Reiniciar todo
docker-compose down  
docker-compose up --build  

---

### Levantar en segundo plano
docker-compose up -d  

---

### Reiniciar solo la app (si falla conexión inicial)
docker restart java-rabbitmq-usercorreo-app-1  

---

### Ver logs de la aplicación
docker logs -f java-rabbitmq-usercorreo-app-1  

---

## ⚠️ Nota importante

Si no se envían correos:
- Verifica que RabbitMQ esté corriendo  
- Revisa logs  
- Puede ser necesario reiniciar la app si RabbitMQ no estaba listo al inicio  

---

## 🎯 Resumen

- App corre en: http://localhost:9100  
- RabbitMQ UI: http://localhost:15672  
- Emails se envían vía Mailtrap (entorno de pruebas)  
