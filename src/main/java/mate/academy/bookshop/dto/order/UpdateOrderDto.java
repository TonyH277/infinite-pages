package mate.academy.bookshop.dto.order;

import jakarta.validation.constraints.NotBlank;

public record UpdateOrderDto(
        @NotBlank
        String status
) {
}
