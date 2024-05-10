package mate.academy.bookshop.repository;

import java.util.Set;
import mate.academy.bookshop.model.Role;
import mate.academy.bookshop.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Set<Role> findByName(RoleName roleUser);
}
