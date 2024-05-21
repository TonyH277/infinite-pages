package mate.academy.bookshop.service;

import java.util.List;
import mate.academy.bookshop.dto.order.OrderItemResponseDto;
import mate.academy.bookshop.dto.order.OrderRequestDto;
import mate.academy.bookshop.dto.order.OrderResponseDto;
import mate.academy.bookshop.dto.order.UpdateOrderDto;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponseDto placeOrder(OrderRequestDto requestDto, Long userId);

    OrderResponseDto update(Long orderId, UpdateOrderDto requestDto);

    List<OrderItemResponseDto> getItems(Long userId, Pageable pageable);

    OrderItemResponseDto getItemByOrderId(Long orderId, Long itemId);

    List<OrderResponseDto> getOrders(Long userId, Pageable pageable);
}
