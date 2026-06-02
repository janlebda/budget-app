package pk.jl.pasir_lebda_jan.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "debts")
public class Debt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;

    private String title;

    public String getTitle() {
        return title != null ? title : "Brak opisu";
    }

    @ManyToOne
    @JoinColumn(name = "debtor_id")
    private User debtor; // User who owes the money

    @ManyToOne
    @JoinColumn(name = "creditor_id")
    private User creditor; // User who is owed the money

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group; // Group to which this debt belongs

    private boolean paidByDebtor = false;
    private boolean confirmedByCreditor = false;
}
