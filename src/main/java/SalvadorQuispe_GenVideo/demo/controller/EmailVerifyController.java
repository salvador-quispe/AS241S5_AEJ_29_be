package SalvadorQuispe_GenVideo.demo.controller;

import SalvadorQuispe_GenVideo.demo.dto.EmailResponse;
import SalvadorQuispe_GenVideo.demo.dto.EmailVerifyRequest;
import SalvadorQuispe_GenVideo.demo.dto.ResponseMapper;
import SalvadorQuispe_GenVideo.demo.dto.UpdateEmailRequest;
import SalvadorQuispe_GenVideo.demo.service.ApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@Tag(name = "Email Verification", description = "Verifica si un email es válido usando WebFlux reactivo")
public class EmailVerifyController {

    private final ApiService apiService;

    @PostMapping("/verify")
    @Operation(summary = "Verificar email", description = "Devuelve si el email es válido o no")
    public Mono<EmailResponse> verify(@Valid @RequestBody EmailVerifyRequest request) {
        return apiService.verifyEmail(request.getEmail())
            .map(ResponseMapper::toEmailResponse);
    }

    @GetMapping("/history")
    @Operation(summary = "Historial de verificaciones activas")
    public Flux<EmailResponse> history() {
        return apiService.obtenerPorTipo("email-verify").map(ResponseMapper::toEmailResponse);
    }

    @GetMapping("/deleted")
    @Operation(summary = "Verificaciones eliminadas", description = "Lista los emails marcados como no admitidos")
    public Flux<EmailResponse> deleted() {
        return apiService.obtenerEliminadosPorTipo("email-verify").map(ResponseMapper::toEmailResponse);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar verificación", description = "Cambia el email y reverifica. Acepta id numérico o uuid")
    public Mono<EmailResponse> update(@PathVariable String id,
                                      @Valid @RequestBody UpdateEmailRequest request) {
        return apiService.updateEmail(id, request.getEmail())
            .map(ResponseMapper::toEmailResponse);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar verificación (borrado lógico)", description = "El email queda como no admitido en el historial")
    public Mono<ResponseEntity<Void>> delete(@PathVariable String id) {
        return apiService.deletePorIdentifier(id)
            .thenReturn(ResponseEntity.<Void>status(HttpStatus.NO_CONTENT).build());
    }

    @PatchMapping("/{id}/restore")
    @Operation(summary = "Restaurar verificación", description = "Reactiva un registro eliminado")
    public Mono<EmailResponse> restore(@PathVariable String id) {
        return apiService.restorePorIdentifier(id)
            .map(ResponseMapper::toEmailResponse);
    }
}
