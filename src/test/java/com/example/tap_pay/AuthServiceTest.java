package com.example.tap_pay;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;


    @Test
    void registerUser_shouldRegisterCustomerSuccessfully() {

        RegisterUserRequest request = new RegisterUserRequest(
                "Khushi",
                "9876543210",
                UserRole.ADMIN,
                "password123"
        );

        UUID userId = UUID.randomUUID();

        when(userRepository.findByPhoneNumber("9876543210"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setId(userId);
        savedUser.setName("Khushi");
        savedUser.setPhoneNumber("9876543210");
        savedUser.setPassword("encodedPassword");
        savedUser.setRole(UserRole.CUSTOMER);
        savedUser.setActive(true);

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        when(jwtService.generateToken(savedUser))
                .thenReturn("test-jwt-token");

        AuthResponse response = authService.registerUser(request);

        assertNotNull(response);
        assertEquals(userId, response.userId());
        assertEquals("Khushi", response.name());
        assertEquals("9876543210", response.phoneNumber());

        // New public registrations must become CUSTOMER
        assertEquals(UserRole.CUSTOMER, response.role());

        assertEquals("test-jwt-token", response.token());
        assertEquals("Registration successful", response.message());

        verify(userRepository).findByPhoneNumber("9876543210");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(savedUser);
    }


    @Test
    void registerUser_shouldRejectDuplicatePhoneNumber() {

        RegisterUserRequest request = new RegisterUserRequest(
                "Khushi",
                "9876543210",
                UserRole.CUSTOMER,
                "password123"
        );

        User existingUser = new User();
        existingUser.setId(UUID.randomUUID());
        existingUser.setPhoneNumber("9876543210");

        when(userRepository.findByPhoneNumber("9876543210"))
                .thenReturn(Optional.of(existingUser));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.registerUser(request)
        );

        assertEquals(
                "Phone number already registered",
                exception.getMessage()
        );

        verify(userRepository).findByPhoneNumber("9876543210");

        verify(passwordEncoder, never())
                .encode(any());

        verify(userRepository, never())
                .save(any(User.class));

        verify(jwtService, never())
                .generateToken(any(User.class));
    }


    @Test
    void login_shouldLoginSuccessfully() {

        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setName("Khushi");
        user.setPhoneNumber("9876543210");
        user.setPassword("encodedPassword");
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);

        LoginRequest request = new LoginRequest(
                "9876543210",
                "password123"
        );

        when(userRepository.findByPhoneNumber("9876543210"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "password123",
                "encodedPassword"
        )).thenReturn(true);

        when(jwtService.generateToken(user))
                .thenReturn("login-jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals(userId, response.userId());
        assertEquals("Khushi", response.name());
        assertEquals("9876543210", response.phoneNumber());
        assertEquals(UserRole.CUSTOMER, response.role());
        assertEquals("login-jwt-token", response.token());
        assertEquals("Login successful", response.message());

        verify(userRepository)
                .findByPhoneNumber("9876543210");

        verify(passwordEncoder)
                .matches("password123", "encodedPassword");

        verify(jwtService)
                .generateToken(user);
    }


    @Test
    void login_shouldRejectInvalidPhoneNumber() {

        LoginRequest request = new LoginRequest(
                "9999999999",
                "password123"
        );

        when(userRepository.findByPhoneNumber("9999999999"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.login(request)
        );

        assertEquals(
                "Invalid credentials",
                exception.getMessage()
        );

        verify(passwordEncoder, never())
                .matches(any(), any());

        verify(jwtService, never())
                .generateToken(any(User.class));
    }


    @Test
    void login_shouldRejectInactiveUser() {

        User user = new User();

        user.setId(UUID.randomUUID());
        user.setName("Khushi");
        user.setPhoneNumber("9876543210");
        user.setPassword("encodedPassword");
        user.setRole(UserRole.CUSTOMER);
        user.setActive(false);

        LoginRequest request = new LoginRequest(
                "9876543210",
                "password123"
        );

        when(userRepository.findByPhoneNumber("9876543210"))
                .thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.login(request)
        );

        assertEquals(
                "User account is inactive",
                exception.getMessage()
        );

        verify(passwordEncoder, never())
                .matches(any(), any());

        verify(jwtService, never())
                .generateToken(any(User.class));
    }


    @Test
    void login_shouldRejectWrongPassword() {

        User user = new User();

        user.setId(UUID.randomUUID());
        user.setName("Khushi");
        user.setPhoneNumber("9876543210");
        user.setPassword("encodedPassword");
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);

        LoginRequest request = new LoginRequest(
                "9876543210",
                "wrongPassword"
        );

        when(userRepository.findByPhoneNumber("9876543210"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrongPassword",
                "encodedPassword"
        )).thenReturn(false);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.login(request)
        );

        assertEquals(
                "Invalid credentials",
                exception.getMessage()
        );

        verify(passwordEncoder)
                .matches("wrongPassword", "encodedPassword");

        verify(jwtService, never())
                .generateToken(any(User.class));
    }
}