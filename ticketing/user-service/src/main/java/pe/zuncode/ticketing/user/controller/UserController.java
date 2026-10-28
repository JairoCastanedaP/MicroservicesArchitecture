package pe.zuncode.ticketing.user.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.zuncode.ticketing.user.dto.RoleUpdateRequest;
import pe.zuncode.ticketing.user.dto.UserResponse;
import pe.zuncode.ticketing.user.service.UserService;

import java.util.List;

/**
 * Endpoints protegidos. Las reglas de acceso están en SecurityConfig:
 * {@code /me} solo requiere un token válido; los demás, rol ADMIN.
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // @AuthenticationPrincipal inyecta el token ya validado; su "sub" es el correo del usuario
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return userService.findByEmail(jwt.getSubject());
    }

    @GetMapping
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    // Por ejemplo, convertir a un cliente en ORGANIZER. El cambio se ve en su PRÓXIMO token
    @PatchMapping("/{id}/role")
    public UserResponse changeRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        return userService.changeRole(id, request.role());
    }
}
