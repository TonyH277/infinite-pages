package mate.academy.bookshop.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import mate.academy.bookshop.model.Book;
import mate.academy.bookshop.model.order.Order;
import mate.academy.bookshop.model.order.OrderItem;
import mate.academy.bookshop.repository.book.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:database/orders/setup-before-repository-test.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/orders/cleanup-after-repository.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class OrderItemRepositoryTest {

    private static final int ORDER_ITEM_QUANTITY = 2;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @DisplayName("Find order items by order id")
    @Test
    void findByOrderId_OrderItemsExist_ReturnsPageOfOrderItems() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderItem> orderItemsPage = orderItemRepository.findByOrderId(1L, pageable);

        assertEquals(1, orderItemsPage.getTotalElements());
        OrderItem fetchedOrderItem = orderItemsPage.getContent().get(0);
        assertEquals(1L, fetchedOrderItem.getId());
        assertEquals(ORDER_ITEM_QUANTITY, fetchedOrderItem.getQuantity());
        assertEquals(1L, fetchedOrderItem.getBook().getId());
        assertEquals(1L, fetchedOrderItem.getOrder().getId());
    }
}
