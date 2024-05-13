package mate.academy.bookshop.service;

import mate.academy.bookshop.dto.UserLoginRequestDto;
import mate.academy.bookshop.dto.UserLoginResponseDto;
import mate.academy.bookshop.dto.UserRegistrationRequestDto;
import mate.academy.bookshop.dto.UserResponseDto;
import mate.academy.bookshop.exception.RegistrationException;

public interface AuthenticationService {
    UserResponseDto register(UserRegistrationRequestDto requestDto) throws RegistrationException;

    UserLoginResponseDto login(UserLoginRequestDto request);
}
