package pk.jl.pasir_lebda_jan.service;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import pk.jl.pasir_lebda_jan.repository.UserRepository;
import pk.jl.pasir_lebda_jan.model.User;
import pk.jl.pasir_lebda_jan.security.JwtUtil;
import pk.jl.pasir_lebda_jan.dto.LoginDto;
import pk.jl.pasir_lebda_jan.dto.UserDto;
import pk.jl.pasir_lebda_jan.exception.UserAlreadyExistsException;
import java.util.ArrayList;


@Service
@NullMarked
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    public User register(UserDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Użytkownik z tym emailem już istnieje");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(encoder.encode(dto.getPassword()));
        return userRepository.save(user);
    }

    public String login(LoginDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika"));
        if (!encoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Nieprawidłowe dane logowania");
        }
        return jwtUtil.generateToken(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika" + email));
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), new ArrayList<>());
    }
}
