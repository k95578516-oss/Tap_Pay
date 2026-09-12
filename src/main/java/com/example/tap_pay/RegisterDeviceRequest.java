package com.example.tap_pay;

public record RegisterDeviceRequest(
        String deviceIdentifier,
        String publicKey,
        String keyAlgorithm
) {
    @Override
    public String deviceIdentifier() {
        return deviceIdentifier;
    }

    @Override
    public String keyAlgorithm() {
        return keyAlgorithm;
    }

    @Override
    public String publicKey() {
        return publicKey;
    }
}