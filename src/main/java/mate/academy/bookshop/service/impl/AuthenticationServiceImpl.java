package mate.academy.bookshop.service.impl;

import lombok.RequiredArgsConstructor;
import mate.academy.bookshop.dto.user.UserLoginRequestDto;
import mate.academy.bookshop.dto.user.UserLoginResponseDto;
import mate.academy.bookshop.dto.user.UserRegistrationRequestDto;
import mate.academy.bookshop.dto.user.UserResponseDto;
import mate.academy.bookshop.exception.RegistrationException;
import mate.academy.bookshop.mapper.UserMapper;
import mate.academy.bookshop.model.RoleName;
import mate.academy.bookshop.model.ShoppingCart;
import mate.academy.bookshop.model.User;
import mate.academy.bookshop.repository.RoleRepository;
import mate.academy.bookshop.repository.ShoppingCartRepository;
import mate.academy.bookshop.repository.UserRepository;
import mate.academy.bookshop.service.AuthenticationService;
import mate.academy.bookshop.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final ShoppingCartRepository shoppingCartRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    @Override
    public UserResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new RegistrationException("There is another user with email "
                    + requestDto.getEmail());
        }
        User user = userMapper.toModel(requestDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(roleRepository.findByName(RoleName.ROLE_USER));
        User savedUser = userRepository.save(user);

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUser(savedUser);
        shoppingCartRepository.save(shoppingCart);
        return userMapper.toDto(savedUser);
    }

    @Override
    public UserLoginResponseDto login(UserLoginRequestDto request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        String token = jwtUtil.generateToken(authentication.getName());
        return new UserLoginResponseDto(token);
    }
}
