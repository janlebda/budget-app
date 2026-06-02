package pk.jl.pasir_lebda_jan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DebtDTO {
    @NotNull(message = "id dłużnika nie może być null")
    private Long debtorId;

    @NotNull(message = "id wierzyciela nie może być null")
    private Long creditorId;

    @NotNull(message = "id grupy nie może być null")
    private Long groupId;

    @NotNull(message = "kwota nie może być null")
    @Positive(message = "kwota musi być dodatnia")
    private Double amount;

    @NotBlank(message = "Tytuł nie może być pusty")
    @Size(max = 100, message = "Tytuł nie może być dłuższy niż 100 znaków")
    private String title;
}
