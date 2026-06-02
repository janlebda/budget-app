package pk.jl.pasir_lebda_jan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pk.jl.pasir_lebda_jan.model.Group;
import pk.jl.pasir_lebda_jan.model.User;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByMemberships_User(User user);
}
