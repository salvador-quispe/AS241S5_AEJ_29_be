# Documentación del Backend — AI Hub (TTS + Email Verification)

## Stack técnico
- **Spring Boot 3.4.4** con **Spring WebFlux** (reactivo)
- **R2DBC + PostgreSQL** (Neon cloud) — base de datos reactiva
- **WebClient** para llamadas a APIs externas
- **Springdoc OpenAPI** para Swagger UI
- Puerto: `8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## Base de datos — tabla única `api_requests`

| Campo        | Tipo        | Descripción                                      |
|--------------|-------------|--------------------------------------------------|
| id           | BIGSERIAL   | ID numérico autoincremental                      |
| request_uuid | VARCHAR(100)| UUID único generado por el backend               |
| type         | VARCHAR(20) | `"tts"` o `"email-verify"`                       |
| input_data   | TEXT        | JSON con los parámetros de entrada               |
| result_data  | TEXT        | JSON con la respuesta de la API externa          |
| status       | VARCHAR(20) | `processing` / `completed` / `error` / `deleted` |
| created_at   | TIMESTAMP   | Fecha de creación                                |
| completed_at | TIMESTAMP   | Fecha de finalización                            |

---

## API 1 — Text to Speech (TTS)

### Endpoints

| Método | URL                        | Descripción                              |
|--------|----------------------------|------------------------------------------|
| POST   | `/api/tts/generate`        | Genera audio MP3 desde texto             |
| GET    | `/api/tts/history`         | Lista audios activos                     |
| GET    | `/api/tts/deleted`         | Lista audios eliminados                  |
| GET    | `/api/tts/{id}/audio`      | Descarga el MP3 (acepta id o uuid)       |
| PUT    | `/api/tts/{id}`            | Actualiza texto/voz y regenera el audio  |
| DELETE | `/api/tts/{id}`            | Borrado lógico (status → "deleted")      |
| PATCH  | `/api/tts/{id}/restore`    | Restaura un audio eliminado              |

### POST `/api/tts/generate` — Body
```json
{
  "input": "Today is a wonderful day",
  "model": "tts-1",
  "voice": "alloy",
  "instructions": "Speak in a lively and optimistic tone."
}
```
- `model`: `"tts-1"` | `"tts-1-hd"`
- `voice`: `"alloy"` | `"echo"` | `"fable"` | `"onyx"` | `"nova"` | `"shimmer"`

### PUT `/api/tts/{id}` — Body
```json
{
  "input": "Today is a beautiful sunny day",
  "model": "tts-1",
  "voice": "nova",
  "instructions": "Speak in a calm and clear tone."
}
```

### Respuesta TTS (todos los endpoints excepto descarga)
```json
{
  "id": 5,
  "requestUuid": "130d950a-0a2c-459c-981e-6f3ec481f57d",
  "input": "Today is a wonderful day",
  "model": "tts-1",
  "voice": "alloy",
  "active": true,
  "result": "completed",
  "downloadUrl": "/api/tts/130d950a-0a2c-459c-981e-6f3ec481f57d/audio",
  "createdAt": "2026-04-06T23:43:38"
}
```

### GET `/api/tts/{id}/audio`
- Acepta **id numérico** (`/api/tts/5/audio`) o **uuid** (`/api/tts/130d950a-.../audio`)
- Devuelve el archivo MP3 directamente con `Content-Type: audio/mp3`
- Solo funciona si `result == "completed"`

---

## API 2 — Email Verification

### Endpoints

| Método | URL                          | Descripción                                  |
|--------|------------------------------|----------------------------------------------|
| POST   | `/api/email/verify`          | Verifica si un email es válido               |
| GET    | `/api/email/history`         | Lista verificaciones activas                 |
| GET    | `/api/email/deleted`         | Lista verificaciones eliminadas              |
| PUT    | `/api/email/{id}`            | Cambia el email y reverifica                 |
| DELETE | `/api/email/{id}`            | Borrado lógico (status → "deleted")          |
| PATCH  | `/api/email/{id}/restore`    | Restaura una verificación eliminada          |

### POST `/api/email/verify` — Body
```json
{
  "email": "example@gmail.com"
}
```

### PUT `/api/email/{id}` — Body
```json
{
  "email": "newmail@gmail.com"
}
```

### Respuesta Email (todos los endpoints)
```json
{
  "id": 26,
  "requestUuid": "73cf7e61-3f8a-46b0-bd5b-91c8f4f0cf9c",
  "email": "example@gmail.com",
  "active": true,
  "result": "valid",
  "valid": true,
  "reason": "accepted_email",
  "createdAt": "2026-04-25T01:25:44"
}
```

- `active`: `true` = activo, `false` = eliminado (borrado lógico)
- `result`: `"valid"` | `"invalid"` | `"error"` | `"processing"`
- `valid`: `true` si el email existe y es válido
- `reason`: motivo del resultado (`"accepted_email"`, `"invalid_domain"`, etc.)

---

## Comportamiento del CRUD

| Operación | Comportamiento                                                                 |
|-----------|--------------------------------------------------------------------------------|
| CREATE    | Llama a la API externa, guarda resultado en BD, devuelve DTO limpio            |
| READ      | Historial activo excluye `status = "deleted"`. `/deleted` muestra solo esos    |
| UPDATE    | Actualiza el **mismo registro** (mismo id), vuelve a llamar a la API externa   |
| DELETE    | Borrado lógico: cambia `status` a `"deleted"`, no elimina de la BD             |
| RESTORE   | Cambia `status` de `"deleted"` a `"completed"`                                 |

---

## Guía para crear el Frontend (Angular o React)

### Configuración base
- Base URL del backend: `http://localhost:8080`
- Todos los endpoints devuelven y aceptan `application/json`
- El endpoint de audio devuelve `audio/mp3` (binario)
- No hay autenticación requerida

### Módulo TTS — vistas sugeridas

**1. Formulario de generación**
- Campos: `input` (textarea), `model` (select), `voice` (select), `instructions` (input)
- Al enviar: `POST /api/tts/generate`
- Al recibir respuesta: mostrar botón de descarga con `downloadUrl`

**2. Historial de audios**
- Llamar: `GET /api/tts/history`
- Tabla con columnas: id, texto, voz, resultado, fecha, acciones
- Acciones por fila: Editar (PUT), Eliminar (DELETE), Descargar (GET audio)

**3. Editar audio**
- Formulario pre-cargado con los datos del registro
- Al guardar: `PUT /api/tts/{id}`
- La nueva `downloadUrl` en la respuesta apunta al audio regenerado

**4. Descargar MP3**
```js
// Ejemplo en JavaScript
window.open(`http://localhost:8080/api/tts/${id}/audio`, '_blank');
// o con fetch para blob:
fetch(`http://localhost:8080/api/tts/${id}/audio`)
  .then(r => r.blob())
  .then(blob => {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `audio-${id}.mp3`;
    a.click();
  });
```

### Módulo Email — vistas sugeridas

**1. Formulario de verificación**
- Campo: `email` (input type email)
- Al enviar: `POST /api/email/verify`
- Mostrar resultado con badge de color: verde = valid, rojo = invalid

**2. Historial de verificaciones**
- Llamar: `GET /api/email/history`
- Tabla con columnas: id, email, resultado, razón, activo, fecha, acciones
- Acciones: Editar (PUT), Eliminar (DELETE)
- Badge visual: `valid` → verde, `invalid` → rojo, `error` → amarillo

**3. Papelera / eliminados**
- Llamar: `GET /api/email/deleted`
- Mostrar registros con opción de Restaurar (`PATCH /{id}/restore`)

### Manejo de estados en el frontend

```
result == "processing"  → spinner / cargando
result == "completed"   → éxito (TTS: mostrar botón descarga)
result == "valid"       → email válido (verde)
result == "invalid"     → email inválido (rojo)
result == "error"       → error de API (amarillo)
active == false         → registro eliminado (gris / tachado)
```

### Ejemplo de llamadas con fetch (JavaScript)

```js
const BASE = 'http://localhost:8080';

// CREATE email
const crear = (email) =>
  fetch(`${BASE}/api/email/verify`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email })
  }).then(r => r.json());

// READ historial
const listar = () =>
  fetch(`${BASE}/api/email/history`).then(r => r.json());

// UPDATE email
const actualizar = (id, email) =>
  fetch(`${BASE}/api/email/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email })
  }).then(r => r.json());

// DELETE lógico
const eliminar = (id) =>
  fetch(`${BASE}/api/email/${id}`, { method: 'DELETE' });

// RESTORE
const restaurar = (id) =>
  fetch(`${BASE}/api/email/${id}/restore`, { method: 'PATCH' }).then(r => r.json());

// CREATE audio TTS
const generarAudio = (data) =>
  fetch(`${BASE}/api/tts/generate`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  }).then(r => r.json());

// DESCARGAR MP3
const descargarAudio = (id) =>
  window.open(`${BASE}/api/tts/${id}/audio`, '_blank');
```

---

## Prompt para crear el frontend con IA

```
Crea un frontend en [Angular/React] que consuma este backend Spring WebFlux en http://localhost:8080.

El backend tiene 2 módulos:

1. TEXT TO SPEECH (TTS):
   - POST /api/tts/generate → { input, model, voice, instructions } → genera audio MP3
   - GET /api/tts/history → lista audios activos
   - GET /api/tts/deleted → lista audios eliminados
   - GET /api/tts/{id}/audio → descarga MP3 (acepta id numérico)
   - PUT /api/tts/{id} → { input, model, voice, instructions } → regenera audio
   - DELETE /api/tts/{id} → borrado lógico
   - PATCH /api/tts/{id}/restore → restaura eliminado

   Respuesta: { id, requestUuid, input, model, voice, active, result, downloadUrl, createdAt }

2. EMAIL VERIFICATION:
   - POST /api/email/verify → { email } → verifica si es válido
   - GET /api/email/history → lista verificaciones activas
   - GET /api/email/deleted → lista eliminadas
   - PUT /api/email/{id} → { email } → reverifica con nuevo email
   - DELETE /api/email/{id} → borrado lógico
   - PATCH /api/email/{id}/restore → restaura eliminado

   Respuesta: { id, requestUuid, email, active, result, valid, reason, createdAt }

Implementa CRUD completo con:
- Formulario para crear (C)
- Tabla con historial (R) con paginación o scroll
- Modal o formulario inline para editar (U)
- Botón eliminar con confirmación (D)
- Sección de papelera con botón restaurar
- Para TTS: botón de descarga del MP3 en cada fila
- Badges de color según result: valid=verde, invalid=rojo, completed=azul, error=amarillo
- Manejo de errores con mensajes al usuario
- CORS ya está habilitado en el backend
```
