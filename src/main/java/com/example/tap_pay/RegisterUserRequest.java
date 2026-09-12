package com.example.tap_pay;

import org.jspecify.annotations.Nullable;

public record RegisterUserRequest(
        String name,
        String phoneNumber,
        UserRole role,
        String password
) {
    @Override
    public String name() {
        return name;
    }

    @Override
    public String phoneNumber() {
        return phoneNumber;
    }

    @Override
    public UserRole role() {
        return role;
    }

    public String password() {
        return password;
    }
}
