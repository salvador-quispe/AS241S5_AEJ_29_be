package SalvadorQuispe_GenVideo.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EmailResponse {
    private Long id;
    private String requestUuid;
    private String email;
    private Boolean active;      // true = activo, false = eliminado (borrado lógico)
    private String result;       // "valid" | "invalid" | "error" | "processing"
    private Boolean valid;       // true si result == "valid"
    private String reason;       // razón del resultado (ej: "invalid_domain")
    private LocalDateTime createdAt;
}
