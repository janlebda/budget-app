package pk.jl.pasir_lebda_jan.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import pk.jl.pasir_lebda_jan.model.Transaction;
import pk.jl.pasir_lebda_jan.model.User;

import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllByUser(User user);
    List<Transaction> findByUser(User user);
    List<Transaction> findAllByUserAndTimestampGreaterThanEqual(User user, LocalDateTime timestamp);
}
