package mate.academy.bookshop.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import mate.academy.bookshop.model.order.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:database/orders/setup-before-repository-test.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/orders/cleanup-after-repository.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class OrderRepositoryTest {

    private static final int ORDER_ITEM_QUANTITY = 2;

    @Autowired
    private OrderRepository orderRepository;

    @DisplayName("Find order by id with items and books")
    @Test
    @Transactional
    void findById_OrderExists_ReturnsOrderWithItemsAndBooks() {
        Optional<Order> response = orderRepository.findById(1L);

        assertTrue(response.isPresent());
        Order fetchedOrder = response.get();
        assertEquals(1L, fetchedOrder.getId());
        assertEquals(1, fetchedOrder.getOrderItems().size());
        assertEquals(1L, fetchedOrder.getOrderItems()
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
        List<Order> orders = orderRepository.findAllByUserId(1L, pageable);

        assertEquals(1, orders.size());
        Order fetchedOrder = orders.get(0);
        assertEquals(1L, fetchedOrder.getId());
        assertEquals(1, fetchedOrder.getOrderItems().size());
        assertEquals(1L, fetchedOrder.getOrderItems()
                .iterator()
                .next()
                .getBook()
                .getId());
    }
}
