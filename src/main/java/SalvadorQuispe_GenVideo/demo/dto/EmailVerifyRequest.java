package SalvadorQuispe_GenVideo.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request para verificar un email")
public class EmailVerifyRequest {

    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "Formato de email inválido")
    @Schema(description = "Email a verificar", example = "example@gmail.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String email = "example@gmail.com";
}
