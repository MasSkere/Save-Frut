# ─── Etapa 1: Construcción (Builder) ───────────────────────────────────────
FROM maven:3.9-amazoncorretto-21 AS builder

WORKDIR /app

# Copiamos la carpeta completa
COPY savefrut-api/ .

RUN mvn clean install -DskipTests

# ─── Etapa 2: Ejecución (Runtime) ──────────────────────────────────────────
FROM amazoncorretto:21-alpine

WORKDIR /app

# Ajuste de ruta según tu captura: api-client -> savefrut-api-client
COPY --from=builder /app/api-client/savefrut-api-client/target/*.jar app.jar

# Copiamos el .env desde la raíz
COPY .env* ./

EXPOSE 8091

CMD ["java", "-jar", "app.jar"]