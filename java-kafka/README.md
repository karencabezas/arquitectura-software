# 📦 Proyecto Kafka Demo - Productor y Consumidor

## 🚀 Despliegue

### Prerrequisitos
- Docker
- Docker Compose

### Pasos

1. Detener contenedores:
docker-compose down -v

2. Limpiar recursos:
docker system prune -f
docker volume prune -f

3. Levantar servicios:
docker-compose up -d

4. Verificar estado:
docker-compose ps

---

## 🧪 Uso

### Productor
docker attach kafka-producer

Selecciona el tópico y envía mensajes.

### Consumidor
docker logs -f kafka-consumer

---

## 🌐 UI Kafka
http://localhost:8080

---

## 🛑 Detener
docker-compose down
