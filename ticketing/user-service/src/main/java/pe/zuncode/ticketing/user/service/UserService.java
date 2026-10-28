package pe.zuncode.ticketing.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.zuncode.ticketing.user.dto.RegisterRequest;
import pe.zuncode.ticketing.user.dto.UserResponse;
import pe.zuncode.ticketing.user.exception.BusinessRuleException;
import pe.zuncode.ticketing.user.exception.ResourceNotFoundException;
import pe.zuncode.ticketing.user.model.Role;
import pe.zuncode.ticketing.user.model.User;
import pe.zuncode.ticketing.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessRuleException("El correo ya está registrado: " + request.email());
        }
        // Todo usuario nuevo es CUSTOMER; un ADMIN puede cambiarle el rol después
        User user = new User(request.email().toLowerCase(), passwordEncoder.encode(request.password()),
                request.fullName(), Role.CUSTOMER, LocalDateTime.now());
        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .map(UserResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + email));
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    @Transactional
    public UserResponse changeRole(Long id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
        user.changeRole(role);
        return UserResponse.from(user);
    }
}
