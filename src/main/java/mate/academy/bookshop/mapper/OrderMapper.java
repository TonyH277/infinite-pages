package mate.academy.bookshop.mapper;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;
import mate.academy.bookshop.config.MapperConfig;
import mate.academy.bookshop.dto.order.OrderRequestDto;
import mate.academy.bookshop.dto.order.OrderResponseDto;
import mate.academy.bookshop.model.CartItem;
import mate.academy.bookshop.model.ShoppingCart;
import mate.academy.bookshop.model.order.Order;
import mate.academy.bookshop.model.order.OrderItem;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class, uses = {OrderItemMapper.class})
public interface OrderMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "orderItems", source = "orderItems", qualifiedByName = "mapOrderItems")
    OrderResponseDto toDto(Order order);

    Order toEntity(OrderRequestDto requestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "price", source = "book.price")
    OrderItem cartItemToOrderItem(CartItem cartItem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "shoppingCart.user")
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "orderDate", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "shippingAddress", source = "requestDto.shippingAddress")
    @Mapping(target = "orderItems", source = "shoppingCart.cartItems",
            qualifiedByName = "mapCartItems")
    Order fromShoppingCart(ShoppingCart shoppingCart, OrderRequestDto requestDto);

    @Named("mapCartItems")
    default Set<OrderItem> mapCartItems(Set<CartItem> cartItems) {
        return cartItems.stream()
                .map(this::cartItemToOrderItem)
                .collect(Collectors.toSet());
    }

    @AfterMapping
    default void setOrderInOrderItems(@MappingTarget Order order) {
        for (OrderItem orderItem : order.getOrderItems()) {
            orderItem.setOrder(order);
        }
        countTotal(order);
    }

    private void countTotal(Order order) {
        BigDecimal total = order.getOrderItems().stream()
                .map(orderItem -> orderItem.getPrice()
                        .multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotal(total);
    }
}
