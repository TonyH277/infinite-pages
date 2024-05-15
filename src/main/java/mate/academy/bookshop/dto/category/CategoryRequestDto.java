package mate.academy.bookshop.dto.category;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequestDto(
        @NotBlank(message = "can not be blank")
        String name,
        String description) {
}
