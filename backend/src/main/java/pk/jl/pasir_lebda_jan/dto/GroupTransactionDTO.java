package pk.jl.pasir_lebda_jan.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupTransactionDTO {
    @NotNull(message = "id grupy nie może być null")
    private Long groupId;

    @NotNull(message = "Kwota nie może być null")
    @Positive(message = "Kwota musi być dodatnia")
    private Double amount;

    @NotBlank(message = "Typ transakcji nie może być pusty")
    @Pattern(regexp = "INCOME|EXPENSE", message = "Typ transakcji musi być INCOME lub EXPENSE")
    private String type;

    @NotBlank(message = "Tytuł nie może być pusty")
    @Size(max = 100, message = "Tytuł nie może być dłuższy niż 100 znaków")
    private String title;

    private List<Long> selectedUserIds;
}
