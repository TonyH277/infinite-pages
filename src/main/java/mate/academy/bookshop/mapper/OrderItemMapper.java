package mate.academy.bookshop.mapper;

import java.util.Set;
import java.util.stream.Collectors;
import mate.academy.bookshop.config.MapperConfig;
import mate.academy.bookshop.dto.order.OrderItemResponseDto;
import mate.academy.bookshop.model.order.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface OrderItemMapper {
    @Mapping(target = "bookId", source = "book.id")
    OrderItemResponseDto toDto(OrderItem item);

    @Named("mapOrderItems")
    default Set<OrderItemResponseDto> mapOrderItems(Set<OrderItem> items) {
        return items.stream()
                .map(this::toDto)
                .collect(Collectors.toSet());
    }
}
