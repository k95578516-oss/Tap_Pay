package com.example.tap_pay;

public interface AuthService {

    AuthResponse registerUser(RegisterUserRequest request);

    AuthResponse login(LoginRequest request);
}