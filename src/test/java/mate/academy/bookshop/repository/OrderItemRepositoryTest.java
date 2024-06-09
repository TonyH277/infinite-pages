package mate.academy.bookshop.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import mate.academy.bookshop.model.Book;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderItemRepositoryTest {

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
    private OrderItemRepository orderItemRepository;
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
        user = new User();
        user.setFirstName(USER_FIRST_NAME);
        user.setLastName(USER_LAST_NAME);
        user.setEmail(USER_EMAIL);
        user.setPassword(USER_PASSWORD);
        user.setRoles(roleRepository.findByName(RoleName.ROLE_USER));
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

    @DisplayName("Find order items by order id")
    @Test
    void findByOrderId_OrderItemsExist_ReturnsPageOfOrderItems() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderItem> orderItemsPage = orderItemRepository.findByOrderId(order.getId(), pageable);

        assertEquals(1, orderItemsPage.getTotalElements());
        OrderItem fetchedOrderItem = orderItemsPage.getContent().get(0);
        assertEquals(orderItem.getId(), fetchedOrderItem.getId());
        assertEquals(ORDER_ITEM_QUANTITY, fetchedOrderItem.getQuantity());
        assertEquals(book.getId(), fetchedOrderItem.getBook().getId());
        assertEquals(order.getId(), fetchedOrderItem.getOrder().getId());
    }
}
