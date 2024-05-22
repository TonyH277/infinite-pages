package mate.academy.bookshop.service.impl;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import mate.academy.bookshop.dto.order.OrderItemResponseDto;
import mate.academy.bookshop.dto.order.OrderRequestDto;
import mate.academy.bookshop.dto.order.OrderResponseDto;
import mate.academy.bookshop.dto.order.UpdateOrderDto;
import mate.academy.bookshop.exception.EmptyShoppingCartException;
import mate.academy.bookshop.exception.EntityNotFoundException;
import mate.academy.bookshop.exception.InvalidStatusException;
import mate.academy.bookshop.mapper.OrderItemMapper;
import mate.academy.bookshop.mapper.OrderMapper;
import mate.academy.bookshop.model.CartItem;
import mate.academy.bookshop.model.ShoppingCart;
import mate.academy.bookshop.model.order.Order;
import mate.academy.bookshop.model.order.OrderItem;
import mate.academy.bookshop.model.order.Status;
import mate.academy.bookshop.repository.CartItemRepository;
import mate.academy.bookshop.repository.OrderItemRepository;
import mate.academy.bookshop.repository.OrderRepository;
import mate.academy.bookshop.repository.ShoppingCartRepository;
import mate.academy.bookshop.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final ShoppingCartRepository shoppingCartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    @Override
    @Transactional
    public OrderResponseDto placeOrder(OrderRequestDto requestDto, Long userId) {
        ShoppingCart userShoppingCart = shoppingCartRepository.findByUserId(userId)
                .orElseThrow(()
                        -> new EntityNotFoundException("No shopping cart for user id " + userId));
        if (userShoppingCart.getCartItems().isEmpty()) {
            throw new EmptyShoppingCartException("Shopping cart is empty for user id " + userId);
        }

        Order order = createOrderFromCart(userShoppingCart, requestDto);
        Order savedOrder = orderRepository.save(order);

        cartItemRepository.deleteAllByShoppingCartId(userShoppingCart.getId());
        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponseDto update(Long orderId, UpdateOrderDto requestDto) {
        Status status = validateStatus(requestDto.status());
        Order order = getOrder(orderId);
        order.setStatus(status);
        Order savedOrder = orderRepository.save(order);
        return orderMapper.toDto(savedOrder);
    }

    @Override
    public List<OrderResponseDto> getOrders(Long userId, Pageable pageable) {
        List<Order> orders = orderRepository.findAllByUserId(userId, pageable);
        return orders.stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    public List<OrderItemResponseDto> getItems(Long orderId, Pageable pageable) {
        if (!orderRepository.existsById(orderId)) {
            throw new EntityNotFoundException("No order with id " + orderId);
        }
        Page<OrderItem> orderItemsPage = orderItemRepository.findByOrderId(orderId, pageable);
        return orderItemsPage.getContent().stream()
                .map(orderItemMapper::toDto)
                .toList();
    }

    @Override
    public OrderItemResponseDto getItemByOrderId(Long orderId, Long itemId) {
        Order order = getOrder(orderId);
        return order.getOrderItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .map(orderItemMapper::toDto)
                .findFirst()
                .orElseThrow(()
                        -> new EntityNotFoundException(String
                        .format("No item with id %d in order with id %d", itemId, orderId)));
    }

    private Order createOrderFromCart(ShoppingCart userShoppingCart, OrderRequestDto requestDto) {
        Order order = orderMapper.toEntity(requestDto);
        Set<OrderItem> orderItems = new HashSet<>();

        for (CartItem item : userShoppingCart.getCartItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setBook(item.getBook());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(item.getBook().getPrice());
            orderItem.setOrder(order);
            orderItems.add(orderItem);
        }

        order.setUser(userShoppingCart.getUser());
        order.setOrderItems(orderItems);
        order.setShippingAddress(requestDto.shippingAddress());
        order.setStatus(Status.PENDING);
        countTotal(order);
        order.setOrderDate(LocalDateTime.now());

        return order;
    }

    private void countTotal(Order order) {
        order.setTotal(order.getOrderItems()
                .stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private Status validateStatus(String status) {
        try {
            return Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidStatusException("Invalid status: " + status);
        }
    }

    private Order getOrder(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(()
                -> new EntityNotFoundException("No order with id " + orderId));
    }
}
