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

    // --- TTS ---
    public Mono<ApiRequest> textToSpeech(String input, String model, String voice, String instructions) {
        ApiRequest req = buildRequest("tts",
            Map.of("input", input, "model", model, "voice", voice,
                   "instructions", instructions != null ? instructions : ""));

        return repository.save(req)
            .flatMap(saved ->
                webClient.post()
                    .uri(ttsUrl)
                    .header("x-rapidapi-key", rapidApiKey)
                    .header("x-rapidapi-host", ttsHost)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                        "model", model,
                        "input", input,
                        "voice", voice,
                        "instructions", instructions != null ? instructions : ""
                    ))
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .map(audioBytes -> {
                        String base64Audio = Base64.getEncoder().encodeToString(audioBytes);
                        saved.setResultData("{\"audio_base64\":\"" + base64Audio
                            + "\",\"content_type\":\"audio/mp3\",\"size_bytes\":" + audioBytes.length + "}");
                        saved.setStatus("completed");
                        saved.setCompletedAt(LocalDateTime.now());
                        return saved;
                    })
                    .onErrorResume(e -> {
                        saved.setStatus("error");
                        saved.setResultData("{\"error\": \"" + e.getMessage().replace("\"", "'") + "\"}");
                        saved.setCompletedAt(LocalDateTime.now());
                        return Mono.just(saved);
                    })
                    .flatMap(repository::save)
            );
    }

    // --- Email Verify ---
    public Mono<ApiRequest> verifyEmail(String email) {
        ApiRequest req = buildRequest("email-verify", Map.of("email", email));

        return repository.save(req)
            .flatMap(saved ->
                webClient.get()
                    .uri(emailUrl + "?email=" + email)
                    .header("x-rapidapi-key", rapidApiKey)
                    .header("x-rapidapi-host", emailHost)
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(body -> {
                        saved.setResultData(body);
                        saved.setStatus("completed");
                        saved.setCompletedAt(LocalDateTime.now());
                        return saved;
                    })
                    .onErrorResume(e -> {
                        saved.setStatus("error");
                        saved.setResultData("{\"error\": \"" + e.getMessage().replace("\"", "'") + "\"}");
                        saved.setCompletedAt(LocalDateTime.now());
                        return Mono.just(saved);
                    })
                    .flatMap(repository::save)
            );
    }

    // --- Consultas ---
    public Mono<ApiRequest> obtenerPorUuid(String uuid) {
        return repository.findByRequestUuid(uuid)
            .switchIfEmpty(Mono.error(new RuntimeException("Request no encontrado: " + uuid)));
    }

    public Flux<ApiRequest> obtenerPorTipo(String type) {
        return repository.findByTypeOrderByCreatedAtDesc(type);
    }

    // --- Helper ---
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
}
