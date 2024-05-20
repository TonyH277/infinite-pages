package mate.academy.bookshop.dto.shoppingcart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCartItemRequestDto(
        @Min(1)
        @NotNull
        int quantity
) {
}
