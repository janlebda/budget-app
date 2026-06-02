package pk.jl.pasir_lebda_jan.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pk.jl.pasir_lebda_jan.exception.EntityNotFoundException;
import pk.jl.pasir_lebda_jan.model.User;
import pk.jl.pasir_lebda_jan.repository.UserRepository;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new AccessDeniedException("Użytkownik nie jest uwierzytelniony");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Nie znaleziono użytkownika o emailu: " + email));
    }
}
