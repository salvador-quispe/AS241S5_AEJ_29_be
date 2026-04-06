package SalvadorQuispe_GenVideo.demo.service;

import SalvadorQuispe_GenVideo.demo.entity.ApiRequest;
import SalvadorQuispe_GenVideo.demo.repository.ApiRequestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiService {

    private final ApiRequestRepository repository;
    private final RestTemplate restTemplate;
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
    public ApiRequest textToSpeech(String input, String model, String voice, String instructions) {
        ApiRequest req = buildRequest("tts",
            Map.of("input", input, "model", model, "voice", voice,
                   "instructions", instructions != null ? instructions : ""));
        ApiRequest saved = repository.save(req);

        try {
            HttpHeaders headers = buildHeaders(ttsHost);
            Map<String, Object> body = Map.of(
                "model", model,
                "input", input,
                "voice", voice,
                "instructions", instructions != null ? instructions : ""
            );

            ResponseEntity<byte[]> response = restTemplate.exchange(
                ttsUrl, HttpMethod.POST,
                new HttpEntity<>(body, headers),
                byte[].class
            );

            byte[] audioBytes = response.getBody();
            if (audioBytes != null && audioBytes.length > 0) {
                // Guardar como base64 para poder devolverlo al cliente
                String base64Audio = java.util.Base64.getEncoder().encodeToString(audioBytes);
                saved.setResultData("{\"audio_base64\":\"" + base64Audio + "\",\"content_type\":\"audio/mp3\",\"size_bytes\":" + audioBytes.length + "}");
            }
            saved.setStatus("completed");
            saved.setCompletedAt(LocalDateTime.now());

        } catch (Exception e) {
            saved.setStatus("error");
            saved.setResultData("{\"error\": \"" + e.getMessage().replace("\"", "'") + "\"}");
            saved.setCompletedAt(LocalDateTime.now());
        }

        return repository.save(saved);
    }

    // --- Email Verify ---
    public ApiRequest verifyEmail(String email) {
        ApiRequest req = buildRequest("email-verify", Map.of("email", email));
        ApiRequest saved = repository.save(req);

        try {
            HttpHeaders headers = buildHeaders(emailHost);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String url = UriComponentsBuilder.fromHttpUrl(emailUrl)
                .queryParam("email", email)
                .toUriString();

            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
            );

            saved.setResultData(response.getBody());
            saved.setStatus("completed");
            saved.setCompletedAt(LocalDateTime.now());

        } catch (Exception e) {
            saved.setStatus("error");
            saved.setResultData("{\"error\": \"" + e.getMessage().replace("\"", "'") + "\"}");
            saved.setCompletedAt(LocalDateTime.now());
        }

        return repository.save(saved);
    }

    // --- Consultas ---
    public ApiRequest obtenerPorUuid(String uuid) {
        return repository.findByRequestUuid(uuid)
            .orElseThrow(() -> new RuntimeException("Request no encontrado: " + uuid));
    }

    public List<ApiRequest> obtenerPorTipo(String type) {
        return repository.findByTypeOrderByCreatedAtDesc(type);
    }

    // --- Helpers ---
    private ApiRequest buildRequest(String type, Map<String, Object> inputData) {
        ApiRequest req = new ApiRequest();
        req.setRequestUuid(UUID.randomUUID().toString());
        req.setType(type);
        req.setStatus("processing");
        try {
            req.setInputData(objectMapper.writeValueAsString(inputData));
        } catch (Exception ignored) {}
        return req;
    }

    private HttpHeaders buildHeaders(String host) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-rapidapi-key", rapidApiKey);
        headers.set("x-rapidapi-host", host);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
