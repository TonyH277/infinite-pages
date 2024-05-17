package mate.academy.bookshop.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import mate.academy.bookshop.anotation.FieldMatch;

@Data
@FieldMatch(first = "password", second = "repeatPassword",
        message = "Password and repeatPassword should match")
public class UserRegistrationRequestDto {
    @NotBlank(message = "can not be blank")
    @Email
    private String email;
    @NotBlank(message = "can not be blank")
    @Size(min = 8, message = "should be at least 8 symbols")
    private String password;
    @NotBlank(message = "can not be blank")
    @Size(min = 8, message = "should be at least 8 symbols")
    private String repeatPassword;
    @NotBlank(message = "can not be blank")
    private String firstName;
    @NotBlank(message = "can not be blank")
    private String lastName;
    private String shippingAddress;
}
