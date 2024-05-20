package mate.academy.bookshop.dto.shoppingcart;

import java.util.Set;

public record CartResponseDto(
        Long id,
        Long userId,
        Set<CartItemDto> cartItems
) {
}
