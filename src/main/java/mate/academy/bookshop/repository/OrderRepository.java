package mate.academy.bookshop.repository;

import java.util.List;
import java.util.Optional;
import mate.academy.bookshop.model.order.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph(value = "orderWithItemsAndBooks",
            type = EntityGraph.EntityGraphType.FETCH)
    @Override
    Optional<Order> findById(Long id);

    @EntityGraph(value = "orderWithItemsAndBooks",
            type = EntityGraph.EntityGraphType.FETCH)
    List<Order> findAllByUserId(Long userId, Pageable pageable);
}
