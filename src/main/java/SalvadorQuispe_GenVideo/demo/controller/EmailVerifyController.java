package SalvadorQuispe_GenVideo.demo.controller;

import SalvadorQuispe_GenVideo.demo.dto.EmailResponse;
import SalvadorQuispe_GenVideo.demo.dto.EmailVerifyRequest;
import SalvadorQuispe_GenVideo.demo.dto.ResponseMapper;
import SalvadorQuispe_GenVideo.demo.service.ApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@Tag(name = "Email Verification", description = "Verifica si un email es válido")
public class EmailVerifyController {

    private final ApiService apiService;

    @PostMapping("/verify")
    @Operation(summary = "Verificar email", description = "Devuelve si el email es válido o no")
    public ResponseEntity<EmailResponse> verify(@Valid @RequestBody EmailVerifyRequest request) {
        return ResponseEntity.ok(
            ResponseMapper.toEmailResponse(apiService.verifyEmail(request.getEmail()))
        );
    }

    @GetMapping("/history")
    @Operation(summary = "Historial de verificaciones")
    public ResponseEntity<List<EmailResponse>> history() {
        return ResponseEntity.ok(
            apiService.obtenerPorTipo("email-verify").stream()
                .map(ResponseMapper::toEmailResponse)
                .toList()
        );
    }
}
