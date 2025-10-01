FROM maven:3.8.6-eclipse-temurin-17 AS build

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos de proyecto Maven
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY mvnw.cmd .

# Descargar todas las dependencias de manera independiente
# Esto mejora el cacheo de Docker para que no descargue dependencias en cada build
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src src

# Compilar la aplicación omitiendo tests para entorno de desarrollo
RUN mvn package -DskipTests

# Crear imagen final mínima para producción
FROM eclipse-temurin:17-jre

WORKDIR /app

# Agregar usuario no-root para mejorar la seguridad
RUN addgroup --system springapp && \
    adduser --system --ingroup springapp springuser

# Copiar el archivo JAR desde la etapa de compilación
COPY --from=build /app/target/*.jar app.jar

# Definir propiedad del archivo JAR para el usuario no-root
RUN chown -R springuser:springapp /app

# Cambiar al usuario no-root
USER springuser

# Definir las variables de entorno necesarias (se pueden sobreescribir)
ENV SPRING_PROFILES_ACTIVE=docker
ENV SERVER_PORT=8080

# Exponer el puerto de la aplicación
EXPOSE 8080

# Configurar punto de entrada para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]