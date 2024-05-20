package mate.academy.bookshop.repository;

import mate.academy.bookshop.model.ShoppingCart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    @EntityGraph(value = "shoppingCartWithItemsAndBooks",
            type = EntityGraph.EntityGraphType.FETCH)
    ShoppingCart findByUserId(Long email);

    boolean existsByUserId(Long userId);
}
