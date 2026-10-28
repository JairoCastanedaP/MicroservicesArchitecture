package pe.zuncode.ticketing.user.dto;

import pe.zuncode.ticketing.user.model.Role;
import pe.zuncode.ticketing.user.model.User;

import java.time.LocalDateTime;

/**
 * Datos públicos de un usuario: nunca exponemos el hash de la contraseña.
 */
public record UserResponse(Long id, String email, String fullName, Role role, LocalDateTime createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole(),
                user.getCreatedAt());
    }
}
