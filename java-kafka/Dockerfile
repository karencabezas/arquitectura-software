FROM maven:3.8.6-openjdk-11-slim AS build

# Directorio de trabajo
WORKDIR /app

# Copiar archivos del proyecto
COPY pom.xml .
COPY src ./src

# Compilar y empaquetar la aplicación
RUN mvn clean package

# Imagen final
FROM eclipse-temurin:11-jre-alpine

# Directorio de trabajo
WORKDIR /app

# Copiar los JARs construidos desde la etapa anterior
COPY --from=build /app/target/producer-jar-with-dependencies.jar /app/producer.jar
COPY --from=build /app/target/consumer-jar-with-dependencies.jar /app/consumer.jar

# Volumen para scripts o configuraciones
VOLUME /app/config

# Establecer punto de entrada (se sobrescribirá en el docker-compose)
ENTRYPOINT ["java", "-jar"]
CMD ["producer.jar"]