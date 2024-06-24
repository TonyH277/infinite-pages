package mate.academy.bookshop.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import mate.academy.bookshop.model.CartItem;
import mate.academy.bookshop.model.ShoppingCart;
import mate.academy.bookshop.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:database/shopping-cart/setup-data-for-repository-test.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/shopping-cart/cleanup-after-repository-test.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ShoppingCartRepositoryTest {

    private static final long EXISTING_USER_ID = 2L;
    private static final long NON_EXISTENT_USER_ID = 999L;

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @DisplayName("Find by user id when shopping cart exists")
    @Test
    void findByUserId_ShoppingCartExists_ReturnsOptionalShoppingCart() {
        Optional<ShoppingCart> response = shoppingCartRepository.findByUserId(EXISTING_USER_ID);

        assertTrue(response.isPresent());
        ShoppingCart fetchedShoppingCart = response.get();
        assertEquals(1L, fetchedShoppingCart.getId());
        assertEquals(1, fetchedShoppingCart.getCartItems().size());

        CartItem cartItem = fetchedShoppingCart.getCartItems().iterator().next();
        assertEquals(1L, cartItem.getId());
        assertEquals(10, cartItem.getQuantity());
    }

    @DisplayName("Find by user id when shopping cart does not exist")
    @Test
    void findByUserId_ShoppingCartDoesNotExist_ReturnsOptionalEmpty() {
        Optional<ShoppingCart> response = shoppingCartRepository.findByUserId(NON_EXISTENT_USER_ID);

        assertFalse(response.isPresent());
    }
}
