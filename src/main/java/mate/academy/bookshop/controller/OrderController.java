package mate.academy.bookshop.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.bookshop.dto.order.OrderItemResponseDto;
import mate.academy.bookshop.dto.order.OrderRequestDto;
import mate.academy.bookshop.dto.order.OrderResponseDto;
import mate.academy.bookshop.dto.order.UpdateOrderDto;
import mate.academy.bookshop.model.User;
import mate.academy.bookshop.service.OrderService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Order management", description = "Endpoints for managing orders"
        + "credentials for test user = 'user@example.com', 'user' "
        + "credentials for test admin = 'admin@example.com', 'admin'")
@RestController
@RequestMapping("orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @Operation(summary = "Place an order",
            description = "Place an order with items from shopping cart")
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponseDto placeOrder(@RequestBody @Valid OrderRequestDto requestDto,
                                       Authentication authentication) {
        User user = ((User) authentication.getPrincipal());
        return orderService.placeOrder(requestDto, user.getId());
    }

    @Operation(summary = "Get user order history",
            description = "Returns all orders placed by user")
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public List<OrderResponseDto> getOrders(Pageable pageable,
                                            Authentication authentication) {
        User user = ((User) authentication.getPrincipal());
        return orderService.getOrders(user.getId(), pageable);
    }

    @Operation(summary = "Change order status",
            description = "Change order status")
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public OrderResponseDto updateOrder(@PathVariable("id") Long id,
                                        @RequestBody @Valid UpdateOrderDto requestDto) {
        return orderService.update(id, requestDto);
    }

    @Operation(summary = "Get all items from order by id",
            description = "Get all items from order by id")
    @GetMapping("{orderId}/items")
    public List<OrderItemResponseDto> getOrderItems(@PathVariable("orderId") Long orderId,
                                                    Pageable pageable) {
        //pagination here
        return orderService.getItems(orderId, pageable);
    }

    @Operation(summary = "Get item by id from order by id",
            description = "Get item by id from order by id")
    @GetMapping("{orderId}/items/{itemId}")
    public OrderItemResponseDto getOrderItemByOrderId(@PathVariable("orderId") Long orderId,
                                                      @PathVariable("itemId") Long itemId) {
        return orderService.getItemByOrderId(orderId, itemId);
    }
}
