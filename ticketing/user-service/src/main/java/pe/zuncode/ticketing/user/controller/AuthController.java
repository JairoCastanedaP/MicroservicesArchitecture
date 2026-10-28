package pe.zuncode.ticketing.user.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.zuncode.ticketing.user.dto.LoginRequest;
import pe.zuncode.ticketing.user.dto.RegisterRequest;
import pe.zuncode.ticketing.user.dto.TokenResponse;
import pe.zuncode.ticketing.user.dto.UserResponse;
import pe.zuncode.ticketing.user.service.AuthService;
import pe.zuncode.ticketing.user.service.UserService;

import java.net.URI;

/**
 * Endpoints públicos: registrarse e iniciar sesión.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    public AuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse created = userService.register(request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/users/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    // Correo + contraseña → token JWT. Desde aquí el cliente ya no vuelve a enviar su contraseña
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
