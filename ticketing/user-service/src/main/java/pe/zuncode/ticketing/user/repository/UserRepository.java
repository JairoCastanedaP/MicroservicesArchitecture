package pe.zuncode.ticketing.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.zuncode.ticketing.user.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
