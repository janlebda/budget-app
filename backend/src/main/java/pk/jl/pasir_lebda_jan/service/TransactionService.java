package pk.jl.pasir_lebda_jan.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import pk.jl.pasir_lebda_jan.dto.BalanceDTO;
import pk.jl.pasir_lebda_jan.dto.TransactionDTO;
import pk.jl.pasir_lebda_jan.model.Transaction;
import pk.jl.pasir_lebda_jan.model.TransactionType;
import pk.jl.pasir_lebda_jan.model.User;
import pk.jl.pasir_lebda_jan.repository.TransactionRepository;
import pk.jl.pasir_lebda_jan.repository.UserRepository;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        var authentication =SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("No authenticated user found");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));

    }

    public List<Transaction> getAllTransactions(){
        User user = getCurrentUser();
        return transactionRepository.findAllByUser(user);
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
    }

    public BalanceDTO getUserBalance(User user, Float days) {
    List<Transaction> transactions;

    if (days != null) {
        LocalDateTime from = LocalDateTime.now().minusDays(days.longValue());
        transactions = transactionRepository.findAllByUserAndTimestampGreaterThanEqual(user, from);
    } else {
        transactions = transactionRepository.findByUser(user);
    }

    double income = transactions.stream()
            .filter(t -> t.getType() == TransactionType.INCOME)
            .mapToDouble(Transaction::getAmount)
            .sum();

    double expense = transactions.stream()
            .filter(t -> t.getType() == TransactionType.EXPENSE)
            .mapToDouble(Transaction::getAmount)
            .sum();

    return new BalanceDTO(income, expense, income - expense);
}

    public Transaction updateTransaction(Long id, TransactionDTO transactionDTO) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));

        if (!transaction.getUser().getId().equals(getCurrentUser().getId())) {
            throw new RuntimeException("Unauthorized to update this transaction");
        }

        transaction.setAmount(transactionDTO.getAmount());
        transaction.setType(TransactionType.valueOf(transactionDTO.getType()));
        transaction.setTags(transactionDTO.getTags());
        transaction.setNotes(transactionDTO.getNotes());
        return transactionRepository.save(transaction);
    }

    public Transaction createTransaction(TransactionDTO transactionDTO) {
        Transaction transaction = new Transaction();
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setType(TransactionType.valueOf(transactionDTO.getType()));
        transaction.setTags(transactionDTO.getTags());
        transaction.setNotes(transactionDTO.getNotes());
        transaction.setUser(getCurrentUser());
        transaction.setTimestamp(LocalDateTime.now());
        return transactionRepository.save(transaction);
    }

    public Boolean deleteTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
        transactionRepository.delete(transaction);
        return null;
    }

    
}
