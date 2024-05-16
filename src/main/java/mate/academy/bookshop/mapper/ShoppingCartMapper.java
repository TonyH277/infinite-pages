package mate.academy.bookshop.mapper;

import mate.academy.bookshop.config.MapperConfig;
import mate.academy.bookshop.dto.shoppingcart.CartItemRequestDto;
import mate.academy.bookshop.dto.shoppingcart.CartResponseDto;
import mate.academy.bookshop.model.ShoppingCart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class, uses = CartItemMapper.class)
public interface ShoppingCartMapper {
    @Mapping(target = "cartItems", source = "cartItems",
            qualifiedByName = "cartItemDtoFromCartItem")
    @Mapping(target = "userId", source = "user.id")
    CartResponseDto toDto(ShoppingCart shoppingCart);

    ShoppingCart toEntity(CartItemRequestDto requestDto);
}
