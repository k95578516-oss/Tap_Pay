package com.example.tap_pay;

import java.time.LocalDateTime;
import java.util.UUID;

public record DeviceResponse(

        UUID deviceId,

        UUID userId,

        boolean active,

        LocalDateTime registeredAt,

        LocalDateTime lastSeenAt

) {
    @Override
    public UUID deviceId() {
        return deviceId;
    }

    @Override
    public UUID userId() {
        return userId;
    }

    @Override
    public boolean active() {
        return active;
    }

    @Override
    public LocalDateTime lastSeenAt() {
        return lastSeenAt;
    }

    @Override
    public LocalDateTime registeredAt() {
        return registeredAt;
    }
}
