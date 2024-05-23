package mate.academy.bookshop.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mate.academy.bookshop.dto.user.UserRegistrationRequestDto;
import mate.academy.bookshop.dto.user.UserResponseDto;
import mate.academy.bookshop.exception.RegistrationException;
import mate.academy.bookshop.mapper.UserMapper;
import mate.academy.bookshop.model.Role;
import mate.academy.bookshop.model.RoleName;
import mate.academy.bookshop.model.ShoppingCart;
import mate.academy.bookshop.model.User;
import mate.academy.bookshop.repository.RoleRepository;
import mate.academy.bookshop.repository.ShoppingCartRepository;
import mate.academy.bookshop.repository.UserRepository;
import mate.academy.bookshop.service.AuthenticationService;
import mate.academy.bookshop.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ShoppingCartRepository shoppingCartRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private UserRegistrationRequestDto requestDto;
    private User user;
    private UserResponseDto userResponseDto;
    private ShoppingCart shoppingCart;

    @BeforeEach
    void setUp() {
        requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("uniqueEmail@com");
        requestDto.setPassword("password");

        user = new User();
        user.setEmail(requestDto.getEmail());
        user.setPassword(requestDto.getPassword());

        userResponseDto = new UserResponseDto();
        userResponseDto.setEmail(user.getEmail());

        shoppingCart = new ShoppingCart();
        shoppingCart.setUser(user);
    }


    @Test
    void register_UserWithUniqueEmail_True() throws RegistrationException {
        when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(false);
        when(userMapper.toModel(requestDto)).thenReturn(user);
        when(passwordEncoder.encode(user.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleName.ROLE_USER)).thenReturn(Set.of(new Role()));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userResponseDto);

        UserResponseDto response = authenticationService.register(requestDto);

        verify(userRepository).existsByEmail(requestDto.getEmail());

        assertEquals(userResponseDto, response);

    }

    @Test
    void register_UserWithNotUniqueEmail_ThrowsRegistrationException() {
    }

    @Test
    void register_UserIsSavedToRepository_True() {
    }

    @Test
    void register_ShoppingCartIsCreated_True() {
    }

    @Test
    void register_RolesAreAssigned_True() {
    }

    @Test
    void register_PasswordIsEncoded_True() {
    }

    @Test
    void login_ValidCredentials_ReturnsToken() {
    }

    @Test
    void login_InvalidCredentials_ThrowsAuthenticationException() {
    }
}