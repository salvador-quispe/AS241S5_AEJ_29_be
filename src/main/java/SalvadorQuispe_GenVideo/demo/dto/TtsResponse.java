package SalvadorQuispe_GenVideo.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TtsResponse {
    private Long id;
    private String requestUuid;
    private String input;
    private String model;
    private String voice;
    private Boolean active;      // true = activo, false = eliminado (borrado lógico)
    private String result;       // "completed" | "error" | "processing"
    private String downloadUrl;  // disponible cuando result == "completed"
    private LocalDateTime createdAt;
}
