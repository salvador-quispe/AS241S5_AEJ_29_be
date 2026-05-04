package SalvadorQuispe_GenVideo.demo.controller;

import SalvadorQuispe_GenVideo.demo.dto.ResponseMapper;
import SalvadorQuispe_GenVideo.demo.dto.TtsRequest;
import SalvadorQuispe_GenVideo.demo.dto.TtsResponse;
import SalvadorQuispe_GenVideo.demo.dto.UpdateTtsRequest;
import SalvadorQuispe_GenVideo.demo.service.ApiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Base64;

@RestController
@RequestMapping("/api/tts")
@RequiredArgsConstructor
@Tag(name = "Text to Speech", description = "Convierte texto a audio MP3 usando WebFlux reactivo")
public class TtsController {

    private final ApiService apiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/generate")
    @Operation(summary = "Generar audio", description = "Convierte texto a voz y devuelve URL de descarga")
    public Mono<TtsResponse> generate(@Valid @RequestBody TtsRequest request) {
        String model        = request.getModel()        != null ? request.getModel()        : "tts-1";
        String voice        = request.getVoice()        != null ? request.getVoice()        : "alloy";
        String instructions = request.getInstructions() != null ? request.getInstructions() : "";
        return apiService.textToSpeech(request.getInput(), model, voice, instructions)
            .map(ResponseMapper::toTtsResponse);
    }

    @GetMapping("/history")
    @Operation(summary = "Historial de audios activos")
    public Flux<TtsResponse> history() {
        return apiService.obtenerPorTipo("tts").map(ResponseMapper::toTtsResponse);
    }

    @GetMapping("/deleted")
    @Operation(summary = "Audios eliminados", description = "Lista los audios marcados como eliminados")
    public Flux<TtsResponse> deleted() {
        return apiService.obtenerEliminadosPorTipo("tts").map(ResponseMapper::toTtsResponse);
    }

    @GetMapping("/{identifier}/audio")
    @Operation(summary = "Descargar audio MP3", description = "Acepta id numérico o uuid")
    public Mono<ResponseEntity<byte[]>> downloadAudio(@PathVariable String identifier) {
        Mono<SalvadorQuispe_GenVideo.demo.entity.ApiRequest> finder;
        try {
            Long numId = Long.parseLong(identifier);
            finder = apiService.obtenerPorId(numId);
        } catch (NumberFormatException e) {
            finder = apiService.obtenerPorUuid(identifier);
        }

        return finder.map(req -> {
            try {
                JsonNode json = objectMapper.readTree(req.getResultData());
                byte[] audioBytes = Base64.getDecoder().decode(json.get("audio_base64").asText());
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType("audio/mp3"));
                headers.setContentDispositionFormData("attachment", "speech-" + identifier + ".mp3");
                return ResponseEntity.ok().headers(headers).body(audioBytes);
            } catch (Exception e) {
                return ResponseEntity.badRequest().<byte[]>build();
            }
        });
    }

    @PutMapping("/{uuid}")
    @Operation(summary = "Actualizar audio", description = "Edita el texto/voz y regenera el audio llamando a la API")
    public Mono<TtsResponse> update(@PathVariable String uuid,
                                    @Valid @RequestBody UpdateTtsRequest request) {
        String model        = request.getModel()        != null ? request.getModel()        : "tts-1";
        String voice        = request.getVoice()        != null ? request.getVoice()        : "alloy";
        String instructions = request.getInstructions() != null ? request.getInstructions() : "";
        return apiService.updateTts(uuid, request.getInput(), model, voice, instructions)
            .map(ResponseMapper::toTtsResponse);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar audio (borrado lógico)", description = "Acepta id numérico o uuid")
    public Mono<ResponseEntity<Void>> delete(@PathVariable String id) {
        return apiService.deletePorIdentifier(id)
            .thenReturn(ResponseEntity.<Void>status(HttpStatus.NO_CONTENT).build());
    }

    @PatchMapping("/{id}/restore")
    @Operation(summary = "Restaurar audio", description = "Reactiva un registro eliminado")
    public Mono<TtsResponse> restore(@PathVariable String id) {
        return apiService.restorePorIdentifier(id)
            .map(ResponseMapper::toTtsResponse);
    }
}
