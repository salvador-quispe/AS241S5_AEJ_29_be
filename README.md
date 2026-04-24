# salvador-quispe-AS241S5_AEJ_29-be

Microservicio reactivo con **Spring WebFlux** que consume 2 APIs de IA desde RapidAPI, persiste resultados en **PostgreSQL (Neon)** en la nube, y expone endpoints documentados con Swagger.

## Stack

- Java 17 + Spring Boot 3.4.4 (WebFlux / Netty)
- R2DBC + PostgreSQL (Neon Cloud)
- Docker + Kubernetes
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## APIs de IA utilizadas

### 1. OpenAI Text-to-Speech (RapidAPI)
- **Host**: `open-ai-text-to-speech1.p.rapidapi.com`
- **Método**: `POST /`
- **Endpoint local**: `POST /api/tts/generate`

### 2. Validect Email Verification (RapidAPI)
- **Host**: `validect-email-verification-v1.p.rapidapi.com`
- **Método**: `GET /v1/verify?email={email}`
- **Endpoint local**: `POST /api/email/verify`

## Imagen Docker

```
docker pull salvaqc/salvador-quispe-genvideo:latest
```

## Despliegue en Kubernetes

```bash
kubectl apply -f manifest-salvador-quispe/salvador-quispe-29-namespace.yml
kubectl apply -f manifest-salvador-quispe/salvador-quispe-29-secret.yml
kubectl apply -f manifest-salvador-quispe/salvador-quispe-29-service.yml
kubectl apply -f manifest-salvador-quispe/salvador-quispe-29-deployment.yml
```

Acceder via port-forward:
```bash
kubectl port-forward -n salvador-quispe-29 deployment/salvador-quispe-29-deployment 8080:8080
```

---

## Demo de dependencia del Secret (video de entrega)

### INICIO — Levantar el port-forward
```bash
kubectl port-forward -n salvador-quispe-29 deployment/salvador-quispe-29-deployment 8080:8080
```
> Dejar corriendo en una terminal y abrir `http://localhost:8080/swagger-ui.html`

### PARTE 1 — Verificar que funciona
```bash
kubectl get all -n salvador-quispe-29
kubectl get pods -n salvador-quispe-29
```

### PARTE 2 — Eliminar secret y deployment
```bash
kubectl delete -f manifest-salvador-quispe/salvador-quispe-29-secret.yml
kubectl delete -f manifest-salvador-quispe/salvador-quispe-29-deployment.yml
```

### PARTE 3 — Crear solo el deployment (sin secret)
```bash
kubectl apply -f manifest-salvador-quispe/salvador-quispe-29-deployment.yml
```

### PARTE 4 — Mostrar que falla
```bash
kubectl get pods -n salvador-quispe-29
kubectl describe pod -n salvador-quispe-29
```
> El pod queda en estado `CreateContainerConfigError` por dependencia del secret eliminado.

### PARTE 5 — Restaurar todo
```bash
kubectl apply -f manifest-salvador-quispe/salvador-quispe-29-secret.yml
kubectl apply -f manifest-salvador-quispe/salvador-quispe-29-deployment.yml
```

### PARTE 6 — Verificar que funciona de nuevo
```bash
kubectl get pods -n salvador-quispe-29
```
