package pk.jl.pasir_lebda_jan.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nazwa użytkownika jest wymagana")
    private String username;

    @NotBlank(message = "Adres e-mail jest wymagany")
    @Email(message = "Podaj poprawny adres e-mail")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Hasło nie może być puste")
    private String password;

    private String currency = "PLN";
}
