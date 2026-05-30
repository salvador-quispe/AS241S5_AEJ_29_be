# ── Stage 1: build ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

COPY target/*.jar app.jar

# Extraer capas del fat-jar para aprovechar el cache de Docker
RUN java -Djarmode=layertools -jar app.jar extract

# ── Stage 2: runtime ─────────────────────────────────────────────────────────
# gcr.io/distroless/java21-debian12: imagen sin shell, sin paquetes OS innecesarios
# → superficie de ataque mínima, prácticamente 0 CVEs del OS
FROM gcr.io/distroless/java21-debian12:nonroot

WORKDIR /app

# Copiar capas en orden (las menos cambiantes primero → mejor cache)
COPY --from=builder /build/dependencies/ ./
COPY --from=builder /build/spring-boot-loader/ ./
COPY --from=builder /build/snapshot-dependencies/ ./
COPY --from=builder /build/application/ ./

# distroless:nonroot ya corre como usuario no-root (uid 65532)
EXPOSE 8080

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]