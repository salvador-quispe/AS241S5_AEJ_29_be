package SalvadorQuispe_GenVideo.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
@Tag(name = "Test", description = "Endpoints de prueba")
public class TestController {
    
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Verifica que la API esté funcionando")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "OK",
            "message", "API funcionando correctamente"
        ));
    }
    
    @PostMapping("/simulate-video")
    @Operation(summary = "Simular generación de video", description = "Simula la generación de un video sin llamar a API externa")
    public ResponseEntity<Map<String, Object>> simulateVideo(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "video_url", "https://example.com/video-demo.mp4",
            "prompt", request.get("prompt"),
            "message", "Video simulado generado exitosamente"
        ));
    }
}
