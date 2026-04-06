¿1) README.md (pegar tal cual en tu repo salvador-quispe-AS241S5_AEJ_29-be)
Markdown
# salvador-quispe-AS241S5_AEJ_29-be

Microservicio reactivo con **Spring WebFlux** que consume 2 APIs de IA desde RapidAPI, persiste resultados en **PostgreSQL (SQL)** y **MongoDB (NoSQL)** en la nube, y expone endpoints para consultarlos.

## ✅ APIs de IA utilizadas

### 1) OpenAI Text-to-Speech (Swift API)
- **Proveedor**: Swift API (vía RapidAPI)
- **Tipo**: IA – Text-to-Speech
- **Endpoint base**: `https://open-ai-text-to-speech1.p.rapidapi.com`
- **Método**: `POST /text-to-speech`
- **Headers**:
    - `X-RapidAPI-Key: ${RAPIDAPI_KEY}`
    - `X-RapidAPI-Host: open-ai-text-to-speech1.p.rapidapi.com`
- **Request (JSON)**:
{ "text": "Hola, esto es una prueba", "voice": "en-US-1" }

Code
- **Respuesta**: `audio/mpeg` (MP3) o JSON con URL/base64
- **Uso**: enviar texto → recibir audio → guardar metadata/binario
- **Notas**: cuota RapidAPI, latencia ~1-3 s, límite de texto

### 2) Validect Email Verification
- **Proveedor**: PM Tech (Validect) – RapidAPI
- **Tipo**: Validación de correos
- **Endpoint base**: `https://validect-email-verification.p.rapidapi.com`
- **Método**: `GET /v1/validate?email={email}`
- **Headers**:
    - `X-RapidAPI-Key: ${RAPIDAPI_KEY}`
    - `X-RapidAPI-Host: validect-email-verification.p.rapidapi.com`
- **Respuesta (ejemplo)**:
{ "email":"test@example.com","valid":true,"disposable":false,"role":false,"free":true,"score":0.92 }

Code
- **Uso**: validar email → guardar `valid, disposable, score` en BD
- **Notas**: respetar rate limit, no spam

## 🧰 Herramientas y versiones
- **Java**: 17 (Temurin)
- **Spring Boot**: 3.3.x (WebFlux 6.1.x)
- **Persistencia**:
    - SQL: PostgreSQL (R2DBC) – Neon/Supabase/Railway
    - NoSQL: MongoDB Atlas (reactive)
- **Build**: Gradle 8.8 (o Maven 3.9)
- **Contenedor**: Docker (`eclipse-temurin:17-jre-alpine`)

## ▶️ Ejecutar
./gradlew bootRun

o
./mvnw spring-boot:run

Code

## 🔐 Configuración
Ver `src/main/resources/application.yml`. Todas las credenciales se leen desde variables de entorno.
2) src/main/resources/application.yml (credenciales centralizadas)
YAML
spring:
  application:
    name: as241s5-aej-29-be
  r2dbc:
    url: r2dbc:postgresql://${PG_HOST}:${PG_PORT}/${PG_DB}
    username: ${PG_USER}
    password: ${PG_PASS}
  data:
    mongodb:
      uri: ${MONGO_URI} # mongodb+srv://...

rapidapi:
  key: ${RAPIDAPI_KEY}

ai:
  tts:
    base-url: https://open-ai-text-to-speech1.p.rapidapi.com
    host: open-ai-text-to-speech1.p.rapidapi.com
    timeout-ms: 15000
  email:
    base-url: https://validect-email-verification.p.rapidapi.com
    host: validect-email-verification.p.rapidapi.com
    timeout-ms: 15000

server:
  port: 8080
3) Endpoints sugeridos (para el video y pruebas)
POST /api/tts → body { "text": "...", "voice": "en-US-1" } → guarda metadata en PostgreSQL y devuelve URL/bytes.
GET /api/verify?email=... → valida y guarda resultado en MongoDB.
GET /api/results/sql y GET /api/results/nosql → listar lo guardado.
Para dejarlo 100% listo, pásame:
Tu usuario de GitHub (para poner la URL exacta del repo).
El número final ## (ya usé 29 por tu título).
Qué proveedor cloud usarás para Postgres (Neon/Supabase/Railway) y la URI de Mongo Atlas (o dejo placeholders).
Si prefieres Gradle o Maven (te dejo el build.gradle/pom.xml con dependencias).
