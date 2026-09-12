package com.example.tap_pay;

import java.util.UUID;

public record AuthResponse(
        UUID userId,
        String name,
        String phoneNumber,
        UserRole role,
        String token,
        String message
) {
    @Override
    public UUID userId() {
        return userId;
    }

    @Override
    public String phoneNumber() {
        return phoneNumber;
    }

    @Override
    public UserRole role() {
        return role;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String token() {
        return token;
    }

    @Override
    public String message() {
        return message;
    }
}