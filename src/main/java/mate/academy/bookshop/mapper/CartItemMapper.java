package mate.academy.bookshop.mapper;

import java.util.Set;
import java.util.stream.Collectors;
import mate.academy.bookshop.config.MapperConfig;
import mate.academy.bookshop.dto.shoppingcart.CartItemDto;
import mate.academy.bookshop.dto.shoppingcart.CartItemRequestDto;
import mate.academy.bookshop.model.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class, uses = BookMapper.class)
public interface CartItemMapper {

    @Mapping(target = "book", source = "bookId", qualifiedByName = "bookFromId")
    CartItem toEntity(CartItemRequestDto requestDto);

    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "bookTitle", source = "book.title")
    CartItemDto toDto(CartItem cartItem);

    @Named("cartItemDtoFromCartItem")
    default Set<CartItemDto> mapCartItemToDto(Set<CartItem> cartItems) {
        return cartItems.stream()
                .map(this::toDto)
                .collect(Collectors.toSet());
    }
}
