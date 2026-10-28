package pe.zuncode.ticketing.user.dto;

import jakarta.validation.constraints.NotNull;
import pe.zuncode.ticketing.user.model.Role;

public record RoleUpdateRequest(
        @NotNull(message = "es obligatorio")
        Role role
) {
}
