package pk.jl.pasir_lebda_jan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pk.jl.pasir_lebda_jan.model.Membership;

import java.util.List;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {
    List<Membership> findByGroupId(Long groupId);
    boolean existsByGroupIdAndUserId(Long groupId, Long userId);
    void deleteByGroupId(Long groupId);
}
