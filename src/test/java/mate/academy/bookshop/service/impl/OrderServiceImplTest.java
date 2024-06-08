package mate.academy.bookshop.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import mate.academy.bookshop.dto.order.OrderItemResponseDto;
import mate.academy.bookshop.dto.order.OrderRequestDto;
import mate.academy.bookshop.dto.order.OrderResponseDto;
import mate.academy.bookshop.dto.order.UpdateOrderDto;
import mate.academy.bookshop.exception.EmptyShoppingCartException;
import mate.academy.bookshop.exception.EntityNotFoundException;
import mate.academy.bookshop.exception.InvalidStatusException;
import mate.academy.bookshop.mapper.OrderItemMapper;
import mate.academy.bookshop.mapper.OrderMapper;
import mate.academy.bookshop.model.Book;
import mate.academy.bookshop.model.CartItem;
import mate.academy.bookshop.model.ShoppingCart;
import mate.academy.bookshop.model.User;
import mate.academy.bookshop.model.order.Order;
import mate.academy.bookshop.model.order.OrderItem;
import mate.academy.bookshop.model.order.Status;
import mate.academy.bookshop.repository.CartItemRepository;
import mate.academy.bookshop.repository.OrderItemRepository;
import mate.academy.bookshop.repository.OrderRepository;
import mate.academy.bookshop.repository.ShoppingCartRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {
    private static final Long USER_ID = 1L;
    private static final Long CART_ITEM_ID = 1L;
    private static final Long ORDER_ITEM_ID = 1L;
    private static final Long NON_EXISTING_ID = 999L;
    private static final int QUANTITY = 10;
    private static final BigDecimal PRICE = BigDecimal.TEN;
    private static final String TEST_ADDRESS = "Test address";
    private static final String INVALID_STATUS = "Non existing status";
    private static final String STATUS_PENDING = Status.PENDING.name();
    private static final String STATUS_DELIVERED = Status.DELIVERED.name();
    private static final int PAGE = 0;
    private static final int SIZE = 20;

    @Mock
    private ShoppingCartRepository shoppingCartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;

    private ShoppingCart shoppingCart;
    private User user;
    private CartItem cartItem;
    private OrderItemResponseDto itemResponseDto;

    @InjectMocks
    OrderServiceImpl orderService;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(USER_ID);

        cartItem = new CartItem();
        cartItem.setBook(new Book());
        cartItem.setQuantity(QUANTITY);
        cartItem.setShoppingCart(shoppingCart);
        cartItem.setId(CART_ITEM_ID);

        shoppingCart = new ShoppingCart();
        shoppingCart.setUser(user);
        shoppingCart.setId(CART_ITEM_ID);
        shoppingCart.setCartItems(Set.of(cartItem));

        itemResponseDto = new OrderItemResponseDto(
                ORDER_ITEM_ID,
                ORDER_ITEM_ID,
                PRICE,
                QUANTITY
        );
    }

    @Test
    @DisplayName("Place order with valid parameters should return OrderResponseDto")
    public void placeOrder_ValidParams_ReturnsOrderResponseDto() {
        OrderRequestDto requestDto = new OrderRequestDto(TEST_ADDRESS);
        OrderResponseDto expected = new OrderResponseDto(
                ORDER_ITEM_ID,
                user.getId(),
                Set.of(itemResponseDto),
                LocalDateTime.now(),
                PRICE,
                STATUS_PENDING);
        Order order = mapToOrder(shoppingCart, requestDto);
        when(shoppingCartRepository.findByUserId(any(Long.class)))
                .thenReturn(Optional.ofNullable(shoppingCart));
        when(orderMapper.fromShoppingCart(any(ShoppingCart.class), any(OrderRequestDto.class)))
                .thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(any(Order.class))).thenReturn(expected);

        OrderResponseDto actual = orderService.placeOrder(requestDto, user.getId());

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Place order with empty shopping cart"
            + " should throw EmptyShoppingCartException")
    public void placeOrder_EmptyShoppingCart_ThrowsEmptyShoppingCartException() {
        ShoppingCart emptyShoppingCart = new ShoppingCart();
        emptyShoppingCart.setCartItems(new HashSet<>());
        String expected = "Shopping cart is empty for user id " + user.getId();
        when(shoppingCartRepository.findByUserId(any(Long.class)))
                .thenReturn(Optional.ofNullable(emptyShoppingCart));

        EmptyShoppingCartException exception = assertThrows(EmptyShoppingCartException.class, ()
                -> orderService.placeOrder(any(OrderRequestDto.class), user.getId()));
        String actual = exception.getMessage();

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Place order with non-existing shopping cart"
            + " should throw EntityNotFoundException")
    public void placeOrder_NonExistingShoppingCart_ThrowsEntityNotFoundException() {
        when(shoppingCartRepository.findByUserId(any(Long.class)))
                .thenReturn(Optional.empty());
        String expected = "No shopping cart for user id " + user.getId();

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, ()
                -> orderService.placeOrder(any(OrderRequestDto.class), user.getId()));
        String actual = exception.getMessage();

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Update order with valid parameters "
            + "should return OrderResponseDto")
    public void update_ValidParams_ReturnsOrderResponseDto() {
        UpdateOrderDto requestDto = new UpdateOrderDto(STATUS_DELIVERED);
        OrderResponseDto expected = new OrderResponseDto(
                ORDER_ITEM_ID,
                user.getId(),
                Set.of(itemResponseDto),
                LocalDateTime.now(),
                PRICE,
                STATUS_DELIVERED);
        Order order = new Order();
        when(orderRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(any(Order.class))).thenReturn(expected);

        OrderResponseDto actual = orderService.update(any(Long.class), requestDto);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Update order with non-existing order "
            + "should throw EntityNotFoundException")
    public void update_NonExistingOrder_ThrowsEntityNotFoundException() {
        UpdateOrderDto requestDto = new UpdateOrderDto(STATUS_PENDING);
        String expected = "No order with id " + NON_EXISTING_ID;
        when(orderRepository.findById(any(Long.class)))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, ()
                -> orderService.update(NON_EXISTING_ID, requestDto));
        String actual = exception.getMessage();

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Update order with unsupported status"
            + " should throw InvalidStatusException")
    public void update_UnsupportedStatus_ThrowsInvalidStatusException() {
        UpdateOrderDto invalidStatusDto = new UpdateOrderDto(INVALID_STATUS);
        String expected = "Invalid status: " + invalidStatusDto.status();

        InvalidStatusException exception = assertThrows(InvalidStatusException.class, ()
                -> orderService.update(ORDER_ITEM_ID, invalidStatusDto));
        String actual = exception.getMessage();

        assertEquals(expected, actual);
    }

    @Test
    public void getOrders_ValidParams_ReturnsListOfUserOrders() {
        Order order = new Order();
        Pageable pageable = PageRequest.of(PAGE, SIZE);
        OrderResponseDto orderResponseDto = new OrderResponseDto(
                ORDER_ITEM_ID,
                user.getId(),
                Set.of(itemResponseDto),
                LocalDateTime.now(),
                PRICE,
                STATUS_DELIVERED);
        List<OrderResponseDto> expected = List.of(orderResponseDto);
        when(orderRepository.findAllByUserId(user.getId(), pageable))
                .thenReturn(List.of(order));
        when(orderMapper.toDto(any(Order.class)))
                .thenReturn(orderResponseDto);

        List<OrderResponseDto> actual = orderService.getOrders(user.getId(), pageable);

        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get orders with valid parameters "
            + "should return list of user orders")
    public void getItems_ValidParams_ReturnsListOfOrderItemResponseDto() {
        Order order = new Order();
        order.setId(ORDER_ITEM_ID);
        OrderItem orderItem = createOrderItem(order);
        Pageable pageable = PageRequest.of(PAGE, SIZE);
        Page<OrderItem> orderItemPage = new PageImpl<>(List.of(orderItem));
        OrderItemResponseDto itemResponseDto = toDto(orderItem);
        List<OrderItemResponseDto> expected = List.of(itemResponseDto);
        when(orderRepository.existsById(any(Long.class)))
                .thenReturn(true);
        when(orderItemRepository.findByOrderId(order.getId(), pageable))
                .thenReturn(orderItemPage);
        when(orderItemMapper.toDto(any(OrderItem.class)))
                .thenReturn(itemResponseDto);

        List<OrderItemResponseDto> actual = orderService.getItems(order.getId(), pageable);

        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get items with non-existing order ID "
            + "should throw EntityNotFoundException")
    public void getItems_NonExistingOrderId_ThrowsEntityNotFoundException() {
        when(orderRepository.existsById(any(Long.class)))
                .thenReturn(false);
        String expected = "No order with id " + ORDER_ITEM_ID;

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, ()
                -> orderService.getItems(ORDER_ITEM_ID, any(Pageable.class)));
        String actual = exception.getMessage();

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get items with valid parameters "
            + "should return list of OrderItemResponseDto")
    public void getItemByOrderId_ValidParams_ReturnsListOfOrderItemResponseDto() {
        Order order = new Order();
        order.setId(ORDER_ITEM_ID);
        OrderItem orderItem = createOrderItem(order);
        order.setOrderItems(Set.of(orderItem));
        OrderItemResponseDto expected = toDto(orderItem);
        when(orderRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(order));
        when(orderItemMapper.toDto(any(OrderItem.class)))
                .thenReturn(expected);

        OrderItemResponseDto actual = orderService.getItemByOrderId(order.getId(), orderItem.getId());

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get item by order ID with non-existing"
            + " order ID should throw EntityNotFoundException")
    public void getItemByOrderId_NonExistingOrderId_ThrowsEntityNotFoundException() {
        when(orderRepository.findById(any(Long.class)))
                .thenReturn(Optional.empty());
        String expected = "No order with id " + NON_EXISTING_ID;

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, ()
                -> orderService.getItemByOrderId(NON_EXISTING_ID, ORDER_ITEM_ID));
        String actual = exception.getMessage();

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get item by order ID with non-existing"
            + " item ID should throw EntityNotFoundException")
    public void getItemByOrderId_NonExistingItemId_ThrowsEntityNotFoundException() {
        Order order = new Order();
        order.setId(ORDER_ITEM_ID);
        OrderItem orderItem = createOrderItem(order);
        order.setOrderItems(Set.of(orderItem));
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        String expected = String.format("No item with id %d in order with id %d",
                NON_EXISTING_ID,
                order.getId());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, ()
                -> orderService.getItemByOrderId(order.getId(), NON_EXISTING_ID));
        String actual = exception.getMessage();

        assertEquals(expected, actual);
    }

    private Order mapToOrder(ShoppingCart shoppingCart, OrderRequestDto requestDto) {
        Order order = new Order();
        order.setUser(shoppingCart.getUser());
        order.setOrderDate(LocalDateTime.now());
        order.setShippingAddress(requestDto.shippingAddress());
        order.setStatus(Status.PENDING);
        order.setTotal(PRICE);
        order.setOrderItems(Set.of(new OrderItem()));
        return order;
    }

    private OrderItemResponseDto toDto(OrderItem orderItem) {
        return new OrderItemResponseDto(
                orderItem.getId(),
                orderItem.getBook().getId(),
                orderItem.getPrice(),
                orderItem.getQuantity());
    }

    private OrderItem createOrderItem(Order order) {
        Book book = new Book();
        book.setId(ORDER_ITEM_ID);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setPrice(PRICE);
        orderItem.setBook(new Book());
        orderItem.setQuantity(QUANTITY);
        orderItem.setId(ORDER_ITEM_ID);
        return orderItem;
    }
}
