package pe.zuncode.ticketing.user.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.zuncode.ticketing.user.dto.LoginRequest;
import pe.zuncode.ticketing.user.dto.TokenResponse;
import pe.zuncode.ticketing.user.exception.ResourceNotFoundException;
import pe.zuncode.ticketing.user.model.User;
import pe.zuncode.ticketing.user.repository.UserRepository;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository,
                       TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        // Verifica correo y contraseña. Si no coinciden lanza BadCredentialsException (→ 401)
        authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password()));

        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + request.email()));
        return tokenService.issue(user);
    }
}
