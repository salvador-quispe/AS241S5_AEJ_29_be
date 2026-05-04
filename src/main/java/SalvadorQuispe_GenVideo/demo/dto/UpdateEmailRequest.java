package SalvadorQuispe_GenVideo.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request para actualizar y reverificar un email")
public class UpdateEmailRequest {

    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "Formato de email inválido")
    @Schema(description = "Nuevo email a verificar", example = "newmail@gmail.com")
    private String email;
}
