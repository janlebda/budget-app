package pk.jl.pasir_lebda_jan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupDTO {

    @NotBlank(message = "Nazwa grupy nie może być pusta")
    @Size(max = 100, message = "Nazwa grupy nie może być dłuższa niż 100 znaków")
    private String name;
}
