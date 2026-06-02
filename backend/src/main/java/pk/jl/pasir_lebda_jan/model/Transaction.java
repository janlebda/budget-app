package pk.jl.pasir_lebda_jan.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions")

@SuppressWarnings("JpaDataSourceORMInspection")
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private String tags;

    private String notes;

    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Transaction(Double amount, TransactionType type, String tags, String notes, LocalDateTime timestamp, User user) {
        this.amount = amount;
        this.type = type;
        this.tags = tags;
        this.notes = notes;
        this.user = user;
        this.timestamp = timestamp; 
    }

}
