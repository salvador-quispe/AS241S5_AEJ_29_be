package SalvadorQuispe_GenVideo.demo.controller;

import SalvadorQuispe_GenVideo.demo.dto.ResponseMapper;
import SalvadorQuispe_GenVideo.demo.dto.TtsRequest;
import SalvadorQuispe_GenVideo.demo.dto.TtsResponse;
import SalvadorQuispe_GenVideo.demo.entity.ApiRequest;
import SalvadorQuispe_GenVideo.demo.service.ApiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/tts")
@RequiredArgsConstructor
@Tag(name = "Text to Speech", description = "Convierte texto a audio MP3")
public class TtsController {

    private final ApiService apiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/generate")
    @Operation(summary = "Generar audio", description = "Convierte texto a voz y devuelve la URL de descarga")
    public ResponseEntity<TtsResponse> generate(@Valid @RequestBody TtsRequest request) {
        String model        = request.getModel()        != null ? request.getModel()        : "tts-1";
        String voice        = request.getVoice()        != null ? request.getVoice()        : "alloy";
        String instructions = request.getInstructions() != null ? request.getInstructions() : "";
        ApiRequest result = apiService.textToSpeech(request.getInput(), model, voice, instructions);
        return ResponseEntity.ok(ResponseMapper.toTtsResponse(result));
    }

    @GetMapping("/history")
    @Operation(summary = "Historial de audios generados")
    public ResponseEntity<List<TtsResponse>> history() {
        return ResponseEntity.ok(
            apiService.obtenerPorTipo("tts").stream()
                .map(ResponseMapper::toTtsResponse)
                .toList()
        );
    }

    @GetMapping("/{uuid}/audio")
    @Operation(summary = "Descargar audio MP3")
    public ResponseEntity<byte[]> downloadAudio(@PathVariable String uuid) throws Exception {
        ApiRequest req = apiService.obtenerPorUuid(uuid);
        if (!"completed".equals(req.getStatus())) {
            return ResponseEntity.badRequest().build();
        }
        JsonNode json = objectMapper.readTree(req.getResultData());
        byte[] audioBytes = Base64.getDecoder().decode(json.get("audio_base64").asText());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("audio/mp3"));
        headers.setContentDispositionFormData("attachment", "speech-" + uuid + ".mp3");
        return ResponseEntity.ok().headers(headers).body(audioBytes);
    }
}
