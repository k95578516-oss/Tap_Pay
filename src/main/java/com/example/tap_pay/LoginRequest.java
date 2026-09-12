package com.example.tap_pay;

public record LoginRequest(
        String phoneNumber,
        String password
) {
    @Override
    public String phoneNumber() {
        return phoneNumber;
    }

    @Override
    public String password() {
        return password;
    }
}
