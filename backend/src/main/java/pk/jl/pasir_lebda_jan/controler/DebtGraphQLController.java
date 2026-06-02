package pk.jl.pasir_lebda_jan.controler;

import jakarta.validation.Valid;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import pk.jl.pasir_lebda_jan.dto.DebtDTO;
import pk.jl.pasir_lebda_jan.model.Debt;
import pk.jl.pasir_lebda_jan.service.DebtService;

import java.util.List;

@Controller
public class DebtGraphQLController {

    private final DebtService debtService;

    public DebtGraphQLController(DebtService debtService) {
        this.debtService = debtService;
    }

    @QueryMapping
    public List<Debt> groupDebts(@Argument Long groupId) {
        return debtService.getGroupDebts(groupId);
    }

    @MutationMapping
    public Debt createDebt(@Valid @Argument DebtDTO debtDTO) {
        return debtService.createDebt(debtDTO);
    }

    @MutationMapping
    public Boolean deleteDebt(@Argument Long debtId) {
        debtService.deleteDebt(debtId);
        return true;
    }

    @MutationMapping
public Debt markDebtAsPaid(@Argument Long debtId){
return debtService.markDebtAsPaid(debtId);
}
@MutationMapping
public Debt confirmDebtPayment(@Argument Long debtId){
return debtService.confirmDebtPayment(debtId);
}
}
