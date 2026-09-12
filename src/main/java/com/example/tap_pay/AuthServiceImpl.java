package com.example.tap_pay;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public AuthResponse registerUser(RegisterUserRequest request) {

        // Check whether phone number is already registered
        if (userRepository.findByPhoneNumber(request.phoneNumber()).isPresent()) {
            throw new RuntimeException("Phone number already registered");
        }

        // Create new user
        User user = new User();

        user.setName(request.name());
        user.setPhoneNumber(request.phoneNumber());

        // Store encrypted password
        user.setPassword(
                passwordEncoder.encode(request.password())
        );

        // New users are customers by default
        user.setRole(UserRole.CUSTOMER);

        // Account is active after registration
        user.setActive(true);

        // Save user
        User savedUser = userRepository.save(user);

        // Generate JWT
        String token = jwtService.generateToken(savedUser);

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getPhoneNumber(),
                savedUser.getRole(),
                token,
                "Registration successful"
        );
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        // Find user
        User user = userRepository
                .findByPhoneNumber(request.phoneNumber())
                .orElseThrow(() ->
                        new RuntimeException("Invalid credentials")
                );

        // Check whether account is active
        if (!user.isActive()) {
            throw new RuntimeException("User account is inactive");
        }

        // Verify password
        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword()
        )) {
            throw new RuntimeException("Invalid credentials");
        }

        // Generate JWT
        String token = jwtService.generateToken(user);

        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getPhoneNumber(),
                user.getRole(),
                token,
                "Login successful"
        );
    }
}