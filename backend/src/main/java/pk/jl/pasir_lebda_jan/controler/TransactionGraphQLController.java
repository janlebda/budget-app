package pk.jl.pasir_lebda_jan.controler;

import java.util.List;
import org.springframework.stereotype.Controller;

import jakarta.validation.Valid;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;

import pk.jl.pasir_lebda_jan.dto.BalanceDTO;
import pk.jl.pasir_lebda_jan.dto.TransactionDTO;
import pk.jl.pasir_lebda_jan.model.Transaction;
import pk.jl.pasir_lebda_jan.model.User;
import pk.jl.pasir_lebda_jan.service.TransactionService;



@Controller
public class TransactionGraphQLController {
    private final TransactionService transactionService;
    public TransactionGraphQLController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @QueryMapping
    public List<Transaction> transactions() {
        return transactionService.getAllTransactions();
    }

    @MutationMapping
    public Transaction addTransaction(@Valid @Argument TransactionDTO transactionDTO) {
        return transactionService.createTransaction(transactionDTO);
    }

    @MutationMapping
    public Transaction updateTransaction(@Argument Long id, @Valid @Argument TransactionDTO transactionDTO) {
        return transactionService.updateTransaction(id, transactionDTO);
    }

    @MutationMapping
    public Boolean deleteTransaction(@Argument Long id) {
        return transactionService.deleteTransaction(id);
    }

    @QueryMapping
public BalanceDTO userBalance(@Argument Float days) {
    User user = transactionService.getCurrentUser();
    return transactionService.getUserBalance(user, days);
}

}
