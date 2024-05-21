package mate.academy.bookshop.dto.order;

import java.math.BigDecimal;

public record OrderItemResponseDto(
        Long id,
        Long bookId,
        BigDecimal price,
        int quantity
) {
}
