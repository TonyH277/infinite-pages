package mate.academy.bookshop.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import mate.academy.bookshop.model.Book;
import mate.academy.bookshop.model.Role;
import mate.academy.bookshop.model.RoleName;
import mate.academy.bookshop.model.User;
import mate.academy.bookshop.model.order.Order;
import mate.academy.bookshop.model.order.OrderItem;
import mate.academy.bookshop.model.order.Status;
import mate.academy.bookshop.repository.book.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderRepositoryTest {

    private static final String USER_FIRST_NAME = "test";
    private static final String USER_LAST_NAME = "test";
    private static final String USER_EMAIL = "test@example.com";
    private static final String USER_PASSWORD = "password";
    private static final String BOOK_TITLE = "Test Book";
    private static final String BOOK_AUTHOR = "Test Author";
    private static final String BOOK_ISBN = "123456789";
    private static final BigDecimal BOOK_PRICE = BigDecimal.TEN;
    private static final int ORDER_ITEM_QUANTITY = 2;

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private RoleRepository roleRepository;

    private User user;
    private Book book;
    private Order order;
    private OrderItem orderItem;

    @BeforeEach
    void setUp() {
        Set<Role> roles = roleRepository.findByName(RoleName.ROLE_USER);

        user = new User();
        user.setRoles(roles);
        user.setFirstName(USER_FIRST_NAME);
        user.setLastName(USER_LAST_NAME);
        user.setEmail(USER_EMAIL);
        user.setPassword(USER_PASSWORD);
        user = userRepository.save(user);

        book = new Book();
        book.setTitle(BOOK_TITLE);
        book.setAuthor(BOOK_AUTHOR);
        book.setIsbn(BOOK_ISBN);
        book.setPrice(BOOK_PRICE);
        book = bookRepository.save(book);

        orderItem = new OrderItem();
        orderItem.setBook(book);
        orderItem.setPrice(BigDecimal.TEN);
        orderItem.setQuantity(ORDER_ITEM_QUANTITY);

        order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Status.PENDING);
        order.setTotal(BigDecimal.TEN);
        order.setShippingAddress("test");

        Set<OrderItem> orderItems = new HashSet<>();
        orderItems.add(orderItem);
        order.setOrderItems(orderItems);

        orderItem.setOrder(order);
        order = orderRepository.save(order);
    }

    @DisplayName("Find order by id with items and books")
    @Test
    @Transactional
    void findById_OrderExists_ReturnsOrderWithItemsAndBooks() {
        Optional<Order> response = orderRepository.findById(order.getId());

        assertTrue(response.isPresent());
        Order fetchedOrder = response.get();
        assertEquals(order.getId(), fetchedOrder.getId());
        assertEquals(1, fetchedOrder.getOrderItems().size());
        assertEquals(book.getId(), fetchedOrder.getOrderItems()
                .iterator()
                .next()
                .getBook()
                .getId());
    }

    @DisplayName("Find all orders by user id with items and books")
    @Test
    @Transactional
    void findAllByUserId_OrdersExist_ReturnsOrdersWithItemsAndBooks() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orders = orderRepository.findAllByUserId(user.getId(), pageable);

        assertEquals(1, orders.size());
        Order fetchedOrder = orders.get(0);
        assertEquals(order.getId(), fetchedOrder.getId());
        assertEquals(1, fetchedOrder.getOrderItems().size());
        assertEquals(book.getId(), fetchedOrder.getOrderItems()
                .iterator()
                .next()
                .getBook()
                .getId());
    }
}
