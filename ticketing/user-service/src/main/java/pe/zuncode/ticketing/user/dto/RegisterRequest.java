package pe.zuncode.ticketing.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos para registrarse. Todo usuario nuevo es CUSTOMER: el rol NO lo elige el cliente
 * (si lo pudiera enviar, cualquiera se registraría como ADMIN).
 */
public record RegisterRequest(
        @NotBlank(message = "es obligatorio")
        @Email(message = "debe ser un correo válido")
        @Size(max = 150, message = "debe tener como máximo {max} caracteres")
        String email,

        @NotBlank(message = "es obligatoria")
        @Size(min = 8, max = 72, message = "debe tener entre {min} y {max} caracteres")
        String password,

        @NotBlank(message = "es obligatorio")
        @Size(max = 120, message = "debe tener como máximo {max} caracteres")
        String fullName
) {
}
