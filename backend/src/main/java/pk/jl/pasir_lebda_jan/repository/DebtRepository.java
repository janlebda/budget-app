package pk.jl.pasir_lebda_jan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pk.jl.pasir_lebda_jan.model.Debt;

import java.util.List;

@Repository
public interface DebtRepository extends JpaRepository<Debt, Long> {
    List<Debt> findByGroupId(Long groupId);
    void deleteByGroupId(Long groupId);
}
