package pk.jl.pasir_lebda_jan.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class TransactionDTO {
    @NotNull(message = "Kwota nie może być pusta")
    @Min(value = 1, message = "Kwota musi być większa niż 0")
    private Double amount;

    @NotNull(message = "Typ wymagany")
    @Pattern(regexp = "INCOME|EXPENSE", message = "Typ musi być Wydatkiem albo Przychodem")
    private String type;

    @Size(max = 50, message = "Tagi nie mogą być dłuższe niż 50 znaków")
    private String tags;
    @Size(max = 255, message = "Notatki nie mogą być dłuższe niż 255 znaków")
    private String notes;


}
