FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /workspace/app

# Copiar el proyecto Maven
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Hacer el archivo mvnw ejecutable
RUN chmod +x ./mvnw

# Construir el proyecto, omitiendo pruebas para acelerar el build
RUN ./mvnw package -DskipTests

# Usar una imagen más ligera para la aplicación final
FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp

# Instalar dependencias requeridas
RUN apk add --no-cache bash

# Crear directorio para la aplicación
RUN mkdir /app

# Copiar el JAR desde la fase de build
COPY --from=build /workspace/app/target/*.jar /app/app.jar

# Cambiar al directorio de la aplicación
WORKDIR /app

# Exponer el puerto de la aplicación
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]