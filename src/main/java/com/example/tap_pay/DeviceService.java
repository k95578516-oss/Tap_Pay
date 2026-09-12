package com.example.tap_pay;

import java.util.UUID;

public interface DeviceService {

    DeviceResponse registerDevice(
            UUID userId,
            RegisterDeviceRequest request
    );

    DeviceResponse getDevice(UUID deviceId);

    DeviceResponse getActiveDevice(UUID deviceId);

    void deactivateDevice(UUID deviceId);

    void updateLastSeen(UUID deviceId);
}