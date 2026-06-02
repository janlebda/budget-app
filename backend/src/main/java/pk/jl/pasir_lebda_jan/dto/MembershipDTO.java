package pk.jl.pasir_lebda_jan.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MembershipDTO {
    @NotBlank(message = "Email nie może być pusty")
    @Email(message = "Nieprawidłowy format email")
    private String userEmail;

    @NotNull(message = "id grupy nie może być null")
    private Long groupId;
}
