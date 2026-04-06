package SalvadorQuispe_GenVideo.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request para convertir texto a voz")
public class TtsRequest {

    @NotBlank(message = "El texto no puede estar vacío")
    @Schema(description = "Texto a convertir en audio", example = "Today is a wonderful day",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String input = "Today is a wonderful day";

    @Schema(description = "Modelo TTS", example = "tts-1", defaultValue = "tts-1",
            allowableValues = {"tts-1", "tts-1-hd"})
    private String model = "tts-1";

    @Schema(description = "Voz a utilizar", example = "alloy", defaultValue = "alloy",
            allowableValues = {"alloy", "echo", "fable", "onyx", "nova", "shimmer"})
    private String voice = "alloy";

    @Schema(description = "Instrucciones de tono/estilo", example = "Speak in a lively and optimistic tone.")
    private String instructions = "Speak in a lively and optimistic tone.";
}
