package mate.academy.bookshop.service;

import mate.academy.bookshop.dto.UserRegistrationRequestDto;
import mate.academy.bookshop.dto.UserResponseDto;
import mate.academy.bookshop.exception.RegistrationException;

public interface AuthenticationService {
    UserResponseDto register(UserRegistrationRequestDto requestDto) throws RegistrationException;
}
