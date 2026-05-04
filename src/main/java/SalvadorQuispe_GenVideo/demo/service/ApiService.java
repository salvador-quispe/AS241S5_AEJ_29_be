package SalvadorQuispe_GenVideo.demo.service;

import SalvadorQuispe_GenVideo.demo.entity.ApiRequest;
import SalvadorQuispe_GenVideo.demo.repository.ApiRequestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiService {

    private final ApiRequestRepository repository;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${rapidapi.key}")
    private String rapidApiKey;

    @Value("${rapidapi.tts.host}")
    private String ttsHost;

    @Value("${rapidapi.tts.url}")
    private String ttsUrl;

    @Value("${rapidapi.email.host}")
    private String emailHost;

    @Value("${rapidapi.email.url}")
    private String emailUrl;

    // ===================== TTS =====================

    public Mono<ApiRequest> textToSpeech(String input, String model, String voice, String instructions) {
        ApiRequest req = buildRequest("tts",
            Map.of("input", input, "model", model, "voice", voice, "instructions", instructions));
        return repository.save(req).flatMap(saved -> callTtsApi(saved, input, model, voice, instructions));
    }

    public Mono<ApiRequest> updateTts(String identifier, String input, String model, String voice, String instructions) {
        return findByIdentifier(identifier)
            .flatMap(existing -> {
                existing.setStatus("processing");
                existing.setCompletedAt(null);
                try {
                    existing.setInputData(objectMapper.writeValueAsString(
                        Map.of("input", input, "model", model, "voice", voice, "instructions", instructions)));
                } catch (Exception ignored) {}
                return repository.save(existing);
            })
            .flatMap(saved -> callTtsApi(saved, input, model, voice, instructions));
    }

    private Mono<ApiRequest> callTtsApi(ApiRequest saved, String input, String model, String voice, String instructions) {
        return webClient.post()
            .uri(ttsUrl)
            .header("x-rapidapi-key", rapidApiKey)
            .header("x-rapidapi-host", ttsHost)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("model", model, "input", input, "voice", voice, "instructions", instructions))
            .retrieve()
            .bodyToMono(byte[].class)
            .flatMap(audioBytes -> {
                String base64 = Base64.getEncoder().encodeToString(audioBytes);
                saved.setResultData("{\"audio_base64\":\"" + base64 +
                    "\",\"content_type\":\"audio/mp3\",\"size_bytes\":" + audioBytes.length + "}");
                saved.setStatus("completed");
                saved.setCompletedAt(LocalDateTime.now());
                return repository.save(saved);
            })
            .onErrorResume(e -> {
                saved.setStatus("error");
                saved.setResultData("{\"error\": \"" + e.getMessage().replace("\"", "'") + "\"}");
                saved.setCompletedAt(LocalDateTime.now());
                return repository.save(saved);
            });
    }

    // ===================== EMAIL =====================

    public Mono<ApiRequest> verifyEmail(String email) {
        ApiRequest req = buildRequest("email-verify", Map.of("email", email));
        return repository.save(req).flatMap(saved -> callEmailApi(saved, email));
    }

    public Mono<ApiRequest> updateEmail(String identifier, String email) {
        return findByIdentifier(identifier)
            .flatMap(existing -> {
                existing.setStatus("processing");
                existing.setCompletedAt(null);
                try {
                    existing.setInputData(objectMapper.writeValueAsString(Map.of("email", email)));
                } catch (Exception ignored) {}
                return repository.save(existing);
            })
            .flatMap(saved -> callEmailApi(saved, email));
    }

    private Mono<ApiRequest> callEmailApi(ApiRequest saved, String email) {
        return webClient.get()
            .uri(emailUrl + "?email=" + email)
            .header("x-rapidapi-key", rapidApiKey)
            .header("x-rapidapi-host", emailHost)
            .retrieve()
            .bodyToMono(String.class)
            .flatMap(body -> {
                saved.setResultData(body);
                saved.setStatus("completed");
                saved.setCompletedAt(LocalDateTime.now());
                return repository.save(saved);
            })
            .onErrorResume(e -> {
                saved.setStatus("error");
                saved.setResultData("{\"error\": \"" + e.getMessage().replace("\"", "'") + "\"}");
                saved.setCompletedAt(LocalDateTime.now());
                return repository.save(saved);
            });
    }

    // ===================== DELETE LÓGICO + RESTORE =====================

    public Mono<Void> deletePorIdentifier(String identifier) {
        return findByIdentifier(identifier)
            .flatMap(existing -> {
                existing.setStatus("deleted");
                return repository.save(existing);
            })
            .then();
    }

    public Mono<ApiRequest> restorePorIdentifier(String identifier) {
        return findByIdentifier(identifier)
            .flatMap(existing -> {
                existing.setStatus("completed");
                return repository.save(existing);
            });
    }

    // ===================== CONSULTAS =====================

    public Mono<ApiRequest> obtenerPorUuid(String uuid) {
        return repository.findByRequestUuid(uuid)
            .switchIfEmpty(Mono.error(new RuntimeException("Request no encontrado: " + uuid)));
    }

    public Mono<ApiRequest> obtenerPorId(Long id) {
        return repository.findById(id)
            .switchIfEmpty(Mono.error(new RuntimeException("Request no encontrado: " + id)));
    }

    public Flux<ApiRequest> obtenerPorTipo(String type) {
        return repository.findByTypeOrderByCreatedAtDesc(type)
            .filter(r -> !"deleted".equals(r.getStatus()));
    }

    public Flux<ApiRequest> obtenerEliminadosPorTipo(String type) {
        return repository.findByTypeOrderByCreatedAtDesc(type)
            .filter(r -> "deleted".equals(r.getStatus()));
    }

    // ===================== HELPERS =====================

    private Mono<ApiRequest> findByIdentifier(String identifier) {
        if (isNumeric(identifier)) {
            return repository.findById(Long.parseLong(identifier))
                .switchIfEmpty(Mono.error(new RuntimeException("Request no encontrado: " + identifier)));
        }
        return repository.findByRequestUuid(identifier)
            .switchIfEmpty(Mono.error(new RuntimeException("Request no encontrado: " + identifier)));
    }

    private ApiRequest buildRequest(String type, Map<String, Object> inputData) {
        ApiRequest req = new ApiRequest();
        req.setRequestUuid(UUID.randomUUID().toString());
        req.setType(type);
        req.setStatus("processing");
        req.setCreatedAt(LocalDateTime.now());
        try {
            req.setInputData(objectMapper.writeValueAsString(inputData));
        } catch (Exception ignored) {}
        return req;
    }

    private boolean isNumeric(String s) {
        try { Long.parseLong(s); return true; } catch (NumberFormatException e) { return false; }
    }
}
