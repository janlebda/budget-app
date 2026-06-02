package pk.jl.pasir_lebda_jan.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
public class LoginDto {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
}
