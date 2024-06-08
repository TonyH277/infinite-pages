package mate.academy.bookshop.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ShoppingCartRepositoryTest {

    private static final String USER_FIRST_NAME = "test";
    private static final String USER_LAST_NAME = "test";
    private static final String USER_SHIPPING_ADDRESS = "test";
    private static final String USER_EMAIL = "test@example.com";
    private static final String USER_PASSWORD = "password";
    private static final String BOOK_TITLE = "Test Book";
    private static final String BOOK_AUTHOR = "Test Author";
    private static final String BOOK_ISBN = "123456789";
    private static final BigDecimal BOOK_PRICE = BigDecimal.TEN;
    private static final long NON_EXISTENT_USER_ID = 999L;
    private static final int CART_ITEM_QUANTITY = 10;

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private CartItemRepository cartItemRepository;

    private User user;
    private ShoppingCart shoppingCart;
    private Book book;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        Set<Role> roles = roleRepository.findByName(RoleName.ROLE_USER);

        user = new User();
        user.setRoles(roles);
        user.setFirstName(USER_FIRST_NAME);
        user.setLastName(USER_LAST_NAME);
        user.setShippingAddress(USER_SHIPPING_ADDRESS);
        user.setEmail(USER_EMAIL);
        user.setPassword(USER_PASSWORD);
        user = userRepository.save(user);

        book = new Book();
        book.setTitle(BOOK_TITLE);
        book.setAuthor(BOOK_AUTHOR);
        book.setIsbn(BOOK_ISBN);
        book.setPrice(BOOK_PRICE);
        book = bookRepository.save(book);

        shoppingCart = new ShoppingCart();
        shoppingCart.setUser(user);
        shoppingCart = shoppingCartRepository.save(shoppingCart);

        cartItem = new CartItem();
        cartItem.setBook(book);
        cartItem.setShoppingCart(shoppingCart);
        cartItem.setQuantity(CART_ITEM_QUANTITY);
        cartItem = cartItemRepository.save(cartItem);

        Set<CartItem> cartItems = new HashSet<>();
        cartItems.add(cartItem);
        shoppingCart.setCartItems(cartItems);
        shoppingCart = shoppingCartRepository.save(shoppingCart);
    }

    @DisplayName("Find by user id when shopping cart exists")
    @Test
    void findByUserId_ShoppingCartExists_ReturnsOptionalShoppingCart() {
        Optional<ShoppingCart> response = shoppingCartRepository.findByUserId(user.getId());

        assertTrue(response.isPresent());
        ShoppingCart fetchedShoppingCart = response.get();
        assertEquals(shoppingCart.getId(), fetchedShoppingCart.getId());
        assertEquals(1, fetchedShoppingCart.getCartItems().size());
        assertTrue(fetchedShoppingCart.getCartItems().contains(cartItem));
    }

    @DisplayName("Find by user id when shopping cart does not exist")
    @Test
    void findByUserId_ShoppingCartDoesNotExist_ReturnsOptionalEmpty() {
        Optional<ShoppingCart> response = shoppingCartRepository.findByUserId(NON_EXISTENT_USER_ID);

        assertFalse(response.isPresent());
    }
}
