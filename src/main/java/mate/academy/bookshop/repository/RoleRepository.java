package mate.academy.bookshop.repository;

import mate.academy.bookshop.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
