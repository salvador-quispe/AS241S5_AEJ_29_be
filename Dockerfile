# ── Stage 1: build ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

COPY target/*.jar app.jar

# Extraer capas del fat-jar para aprovechar el cache de Docker
RUN java -Djarmode=layertools -jar app.jar extract

# ── Stage 2: runtime ─────────────────────────────────────────────────────────
# eclipse-temurin:21-jre-alpine: imagen mínima, sin Ubuntu, sin pebble
FROM eclipse-temurin:21-jre-alpine

# Actualizar paquetes del OS Alpine para eliminar CVEs del sistema base
RUN apk update && apk upgrade --no-cache

# Crear usuario no-root para no ejecutar como root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copiar capas en orden (las menos cambiantes primero → mejor cache)
COPY --from=builder /build/dependencies/ ./
COPY --from=builder /build/spring-boot-loader/ ./
COPY --from=builder /build/snapshot-dependencies/ ./
COPY --from=builder /build/application/ ./

# Cambiar propietario al usuario no-root
RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]