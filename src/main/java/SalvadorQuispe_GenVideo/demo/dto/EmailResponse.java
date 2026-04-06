package SalvadorQuispe_GenVideo.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EmailResponse {
    private Long id;
    private String requestUuid;
    private String email;
    private String status;   // "valid", "invalid", "error"
    private Boolean valid;
    private String reason;
    private LocalDateTime createdAt;
}
