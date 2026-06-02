package pk.jl.pasir_lebda_jan.controler;

import java.util.List;

import pk.jl.pasir_lebda_jan.dto.TransactionDTO;
import pk.jl.pasir_lebda_jan.model.Transaction;
import pk.jl.pasir_lebda_jan.repository.TransactionRepository;
import pk.jl.pasir_lebda_jan.service.TransactionService;

import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;






@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping()
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@RequestParam Long id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable Long id, @Valid @RequestBody TransactionDTO transactionDTO) {
        Transaction updatedTransaction = transactionService.updateTransaction(id, transactionDTO);
        return ResponseEntity.ok(updatedTransaction);
    }

    @PostMapping()
    public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody TransactionDTO transactionDTO) {
        Transaction createdTransaction = transactionService.createTransaction(transactionDTO);
        return ResponseEntity.ok(createdTransaction);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}