package pk.jl.pasir_lebda_jan.controler;

import jakarta.validation.Valid;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import pk.jl.pasir_lebda_jan.dto.GroupTransactionDTO;
import pk.jl.pasir_lebda_jan.model.User;
import pk.jl.pasir_lebda_jan.service.CurrentUserService;
import pk.jl.pasir_lebda_jan.service.GroupTransactionService;

@Controller
public class GroupTransactionGraphQLController {

    private final GroupTransactionService groupTransactionService;
    private final CurrentUserService currentUserService;

    public GroupTransactionGraphQLController(
            GroupTransactionService groupTransactionService,
            CurrentUserService currentUserService) {
        this.groupTransactionService = groupTransactionService;
        this.currentUserService = currentUserService;
    }

    @MutationMapping
    public Boolean addGroupTransaction(@Valid @Argument GroupTransactionDTO groupTransactionDTO) {
        User user = currentUserService.getCurrentUser();
        groupTransactionService.addGroupTransaction(groupTransactionDTO, user);
        return true;
    }
}
