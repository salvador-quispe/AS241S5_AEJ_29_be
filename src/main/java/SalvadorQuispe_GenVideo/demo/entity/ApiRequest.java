package SalvadorQuispe_GenVideo.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "api_requests")
@Data
public class ApiRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_uuid", unique = true, length = 100)
    private String requestUuid;

    // "tts" o "email-verify"
    @Column(nullable = false, length = 20)
    private String type;

    // Parámetros de entrada serializados como JSON
    @Column(name = "input_data", columnDefinition = "TEXT")
    private String inputData;

    // Resultado: URL de audio (TTS) o JSON de verificación (email)
    @Column(name = "result_data", columnDefinition = "TEXT")
    private String resultData;

    @Column(length = 20)
    private String status = "pending";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
