package mate.academy.bookshop.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserLoginRequestDto(
        @Email(message = "Invalid email format")
        @NotBlank(message = "Email can not be blank")
        @Size(max = 255, message = "Email must not exceed {max} characters")
        String email,

        @NotBlank(message = "Password can not be blank")
        @Size(min = 4, max = 255, message = "Password must be between {min} and {max} characters")
        String password) {
}
