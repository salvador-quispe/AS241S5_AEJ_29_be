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
        r.setCreatedAt(req.getCreatedAt());
        r.setActive(!"deleted".equals(req.getStatus()));
        r.setResult(req.getStatus()); // completed | error | processing

        // Extraer input/model/voice del inputData
        try {
            JsonNode input = mapper.readTree(req.getInputData());
            r.setInput(input.path("input").asText());
            r.setModel(input.path("model").asText());
            r.setVoice(input.path("voice").asText());
        } catch (Exception ignored) {}

        // URL de descarga directa usando el uuid
        if ("completed".equals(req.getStatus())) {
            r.setDownloadUrl("/api/tts/" + req.getRequestUuid() + "/audio");
        }

        return r;
    }

    public static EmailResponse toEmailResponse(ApiRequest req) {
        EmailResponse r = new EmailResponse();
        r.setId(req.getId());
        r.setRequestUuid(req.getRequestUuid());
        r.setCreatedAt(req.getCreatedAt());
        r.setActive(!"deleted".equals(req.getStatus()));

        // Extraer email del inputData
        try {
            JsonNode input = mapper.readTree(req.getInputData());
            r.setEmail(input.path("email").asText());
        } catch (Exception ignored) {}

        // Intentar leer resultado desde result_data (registros nuevos)
        boolean resolvedFromJson = false;
        if (req.getResultData() != null && !req.getResultData().isBlank()) {
            try {
                JsonNode result = mapper.readTree(req.getResultData());
                String apiStatus = result.path("status").asText("");
                if (!apiStatus.isEmpty()) {
                    r.setResult(apiStatus);
                    r.setValid("valid".equalsIgnoreCase(apiStatus));
                    resolvedFromJson = true;
                }
                if (result.has("reason"))       r.setReason(result.path("reason").asText());
                else if (result.has("message")) r.setReason(result.path("message").asText());
                else if (result.has("error"))   r.setReason(result.path("error").asText());
            } catch (Exception ignored) {}
        }

        // Fallback: registros viejos que tienen "valid"/"invalid" directo en status
        if (!resolvedFromJson) {
            String s = req.getStatus();
            if ("valid".equalsIgnoreCase(s) || "invalid".equalsIgnoreCase(s)) {
                r.setResult(s);
                r.setValid("valid".equalsIgnoreCase(s));
            } else {
                r.setResult(s); // "processing" | "error" | "deleted"
                r.setValid(false);
            }
        }

        return r;
    }
}
