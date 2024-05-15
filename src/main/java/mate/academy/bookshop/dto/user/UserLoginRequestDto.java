package mate.academy.bookshop.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserLoginRequestDto(
        @Email
        @NotBlank(message = "can not be blank")
        String email,

        @NotBlank(message = "can not be blank")
        String password) {
}
