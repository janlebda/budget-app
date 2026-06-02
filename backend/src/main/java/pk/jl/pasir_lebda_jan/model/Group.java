package pk.jl.pasir_lebda_jan.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "`groups`") // `group` is a reserved keyword in SQL, so we need to escape it
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Group name

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner; // Group owner (can invite and remove other users)

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Membership> memberships; // List of group memberships (users in the group)

    @Transient
    public Long getOwnerId() {
        return owner != null ? owner.getId() : null;
    }
}
