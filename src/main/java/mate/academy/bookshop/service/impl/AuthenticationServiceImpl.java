package mate.academy.bookshop.service.impl;

import lombok.RequiredArgsConstructor;
import mate.academy.bookshop.dto.UserRegistrationRequestDto;
import mate.academy.bookshop.dto.UserResponseDto;
import mate.academy.bookshop.exception.RegistrationException;
import mate.academy.bookshop.mapper.UserMapper;
import mate.academy.bookshop.model.RoleName;
import mate.academy.bookshop.model.User;
import mate.academy.bookshop.repository.RoleRepository;
import mate.academy.bookshop.repository.UserRepository;
import mate.academy.bookshop.service.AuthenticationService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

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
        return userMapper.toDto(savedUser);
    }
}
