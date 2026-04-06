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
    private String status;
    private String downloadUrl;
    private LocalDateTime createdAt;
}
