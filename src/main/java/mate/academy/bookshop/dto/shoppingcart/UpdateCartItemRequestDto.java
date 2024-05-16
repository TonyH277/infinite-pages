package mate.academy.bookshop.dto.shoppingcart;

import jakarta.validation.constraints.Min;

public record UpdateCartItemRequestDto(
        @Min(0)
        int quantity
) {
}
