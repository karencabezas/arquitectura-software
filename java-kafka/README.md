# Instrucciones finales para probar la aplicación Kafka

# 1. Detener todos los contenedores
docker-compose down -v

# 2. Asegurarse de eliminar los volúmenes y redes antiguos
docker system prune -f
docker volume prune -f

# 3. Levantar todos los servicios
docker-compose up -d
docker-compose down -v 
# 4. Esperar a que todos los servicios estén activos (importante!)
# Verificar el estado con:
docker-compose ps

# 5. Para enviar mensajes como productor:
docker attach kafka-producer
# (escribe mensajes y presiona Enter, usa 'exit' para salir)

# 6. Para ver los mensajes recibidos por el consumidor (en otra terminal):
docker logs -f kafka-consumer

# 7. Para ver la interfaz gráfica de Kafka:
# Abre en el navegador: http://localhost:8080

# 8. Al terminar, para detener todos los servicios:
docker-compose down