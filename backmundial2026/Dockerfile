# Etapa 1: Build con caché de dependencias
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copiar solo archivos de dependencias primero (para aprovechar caché de Docker)
COPY pom.xml .
COPY src ./src

# Descargar dependencias (esta capa se cachea si pom.xml no cambia)
RUN mvn dependency:go-offline -B

# Compilar (sin tests para mayor velocidad)
RUN mvn clean package -DskipTests -B

# Etapa 2: Runtime más ligera
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiar solo el .war generado
COPY --from=build /app/target/*.war app.war

# Optimizaciones de JVM para contenedor
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+TieredCompilation -XX:TieredStopAtLevel=1"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.war"]