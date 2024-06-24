package mate.academy.bookshop.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import mate.academy.bookshop.model.Book;
import mate.academy.bookshop.model.CartItem;
import mate.academy.bookshop.model.Role;
import mate.academy.bookshop.model.RoleName;
import mate.academy.bookshop.model.ShoppingCart;
import mate.academy.bookshop.model.User;
import mate.academy.bookshop.repository.book.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:database/shopping-cart/setup-data-for-repository-test.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/shopping-cart/cleanup-after-repository-test.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class CartItemRepositoryTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @DisplayName("Delete all by shopping cart id")
    @Test
    @Transactional
    void deleteAllByShoppingCartId_ShoppingCartExists_RemovesAllCartItems() {
        Long shoppingCartId = 1L;

        cartItemRepository.deleteAllByShoppingCartId(shoppingCartId);

        Optional<CartItem> deletedCartItem = cartItemRepository.findById(1L);

        assertFalse(deletedCartItem.isPresent());
    }

    @DisplayName("Delete all by shopping cart id when cart items do not exist")
    @Test
    @Transactional
    void deleteAllByShoppingCartId_ShoppingCartDoesNotExist_NoCartItemsDeleted() {
        Long nonExistentShoppingCartId = 999L;
        cartItemRepository.deleteAllByShoppingCartId(nonExistentShoppingCartId);

        Optional<CartItem> existingCartItem = cartItemRepository.findById(1L);

        assertTrue(existingCartItem.isPresent());
    }
}