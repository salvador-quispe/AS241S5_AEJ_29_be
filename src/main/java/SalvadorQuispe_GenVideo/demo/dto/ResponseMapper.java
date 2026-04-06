package SalvadorQuispe_GenVideo.demo.dto;

import SalvadorQuispe_GenVideo.demo.entity.ApiRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ResponseMapper {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static TtsResponse toTtsResponse(ApiRequest req) {
        TtsResponse r = new TtsResponse();
        r.setId(req.getId());
        r.setRequestUuid(req.getRequestUuid());
        r.setStatus(req.getStatus());
        r.setCreatedAt(req.getCreatedAt());

        // Extraer input/model/voice del inputData
        try {
            JsonNode input = mapper.readTree(req.getInputData());
            r.setInput(input.path("input").asText());
            r.setModel(input.path("model").asText());
            r.setVoice(input.path("voice").asText());
        } catch (Exception ignored) {}

        // URL de descarga si está completado
        if ("completed".equals(req.getStatus())) {
            r.setDownloadUrl("/api/tts/" + req.getRequestUuid() + "/audio");
        }

        return r;
    }

    public static EmailResponse toEmailResponse(ApiRequest req) {
        EmailResponse r = new EmailResponse();
        r.setId(req.getId());
        r.setRequestUuid(req.getRequestUuid());
        r.setStatus(req.getStatus());
        r.setCreatedAt(req.getCreatedAt());

        // Extraer email del inputData
        try {
            JsonNode input = mapper.readTree(req.getInputData());
            r.setEmail(input.path("email").asText());
        } catch (Exception ignored) {}

        // Extraer resultado de la verificación
        try {
            JsonNode result = mapper.readTree(req.getResultData());
            // Validect devuelve { "status": "valid"/"invalid", "reason": "..." }
            String emailStatus = result.path("status").asText();
            r.setValid("valid".equalsIgnoreCase(emailStatus));
            r.setStatus(emailStatus.isEmpty() ? req.getStatus() : emailStatus);

            // Buscar razón en varios campos posibles
            if (result.has("reason"))       r.setReason(result.path("reason").asText());
            else if (result.has("message")) r.setReason(result.path("message").asText());
            else if (result.has("error"))   r.setReason(result.path("error").asText());
        } catch (Exception ignored) {}

        return r;
    }
}
