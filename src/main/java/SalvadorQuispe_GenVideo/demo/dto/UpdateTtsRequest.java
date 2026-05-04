package SalvadorQuispe_GenVideo.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request para actualizar y regenerar un audio TTS")
public class UpdateTtsRequest {

    @NotBlank(message = "El texto no puede estar vacío")
    @Schema(description = "Nuevo texto a convertir en audio", example = "Today is a beautiful sunny day")
    private String input;

    @Schema(description = "Modelo TTS", example = "tts-1", allowableValues = {"tts-1", "tts-1-hd"})
    private String model = "tts-1";

    @Schema(description = "Voz a utilizar", example = "nova",
            allowableValues = {"alloy", "echo", "fable", "onyx", "nova", "shimmer"})
    private String voice = "alloy";

    @Schema(description = "Instrucciones de tono/estilo", example = "Speak in a calm and clear tone.")
    private String instructions = "";
}
