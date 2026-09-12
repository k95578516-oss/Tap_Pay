package com.example.tap_pay;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeviceRepository
        extends JpaRepository<Device, UUID> {

    Optional<Device> findByDeviceIdentifier(
            String deviceIdentifier
    );

    Optional<Device> findByIdAndActiveTrue(UUID id);
}