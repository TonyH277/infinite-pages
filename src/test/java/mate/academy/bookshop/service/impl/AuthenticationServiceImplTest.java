package mate.academy.bookshop.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import mate.academy.bookshop.dto.user.UserLoginRequestDto;
import mate.academy.bookshop.dto.user.UserLoginResponseDto;
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
import mate.academy.bookshop.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
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
        requestDto.setEmail("user@example.com");
        requestDto.setPassword("password");

        user = new User();
        user.setEmail(requestDto.getEmail());
        user.setPassword(requestDto.getPassword());

        userResponseDto = new UserResponseDto();
        userResponseDto.setEmail(user.getEmail());

        shoppingCart = new ShoppingCart();
        shoppingCart.setUser(user);
    }

    @DisplayName("Register new user with valid credentials")
    @Test
    void register_UserWithUniqueEmail_returnsUserResponseDto() throws RegistrationException {
        when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(false);
        when(userMapper.toModel(requestDto)).thenReturn(user);
        when(passwordEncoder.encode(user.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleName.ROLE_USER)).thenReturn(Set.of(new Role()));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userResponseDto);

        UserResponseDto response = authenticationService.register(requestDto);

        assertEquals(userResponseDto, response);
    }

    @DisplayName("Register new user with invalid credentials")
    @Test
    void register_UserWithNotUniqueEmail_ThrowsRegistrationException()
            throws RegistrationException {
        when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(true);

        RegistrationException exception = assertThrows(RegistrationException.class, ()
                -> authenticationService.register(requestDto));

        String expected = "There is another user with email " + requestDto.getEmail();
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }

    @DisplayName("Roles are assigned correctly")
    @Test
    void register_RolesAreAssigned_True() throws RegistrationException {
        Role role = new Role();
        role.setName(RoleName.ROLE_USER);

        when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(false);
        when(userMapper.toModel(requestDto)).thenReturn(user);
        when(passwordEncoder.encode(user.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleName.ROLE_USER)).thenReturn(Set.of(new Role()));
        when(userRepository.save(user)).thenReturn(user);

        authenticationService.register(requestDto);

        verify(roleRepository).findByName(RoleName.ROLE_USER);
        verify(userRepository).save(user);

        assertTrue(user.getRoles().size() == 1);
        assertTrue(user.getRoles().contains(role));
    }

    @DisplayName("Login with valid credentials returns token")
    @Test
    void login_ValidCredentials_ReturnsToken() {
        UserLoginRequestDto loginRequestDto = new UserLoginRequestDto("user@example.com",
                "password");
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                loginRequestDto.email(),
                loginRequestDto.password());

        String expectedToken = "mockedToken";
        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto.email(),
                        loginRequestDto.password())))
                .thenReturn(authentication);
        when(jwtUtil.generateToken(authentication.getName())).thenReturn(expectedToken);

        UserLoginResponseDto response = authenticationService.login(loginRequestDto);

        verify(authenticationManager).authenticate(
                any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(authentication.getName());

        assertEquals(expectedToken, response.token());
    }

    @DisplayName("Login with invalid credentials throws AuthenticationException")
    @Test
    void login_InvalidCredentials_ThrowsAuthenticationException() {
        UserLoginRequestDto loginRequestDto = new UserLoginRequestDto("user@example.com",
                "password");

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(AuthenticationException.class, () ->
                authenticationService.login(loginRequestDto));
    }
}
