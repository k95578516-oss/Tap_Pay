package com.example.tap_pay;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    public DeviceServiceImpl(
            DeviceRepository deviceRepository,
            UserRepository userRepository
    ) {
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
    }


    // =========================
    // REGISTER DEVICE
    // =========================

    @Override
    @Transactional
    public DeviceResponse registerDevice(
            UUID userId,
            RegisterDeviceRequest request
    ) {

        if (userId == null) {
            throw new IllegalArgumentException(
                    "User ID is required"
            );
        }

        if (request == null) {
            throw new IllegalArgumentException(
                    "Device registration request is required"
            );
        }

        if (request.deviceIdentifier() == null
                || request.deviceIdentifier().isBlank()) {

            throw new IllegalArgumentException(
                    "Device identifier is required"
            );
        }

        if (request.publicKey() == null
                || request.publicKey().isBlank()) {

            throw new IllegalArgumentException(
                    "Public key is required"
            );
        }

        if (request.keyAlgorithm() == null
                || request.keyAlgorithm().isBlank()) {

            throw new IllegalArgumentException(
                    "Key algorithm is required"
            );
        }


        // Check whether device identifier is already registered
        if (deviceRepository
                .findByDeviceIdentifier(
                        request.deviceIdentifier()
                )
                .isPresent()) {

            throw new IllegalStateException(
                    "Device is already registered"
            );
        }


        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );


        // User must be active
        if (!user.isActive()) {

            throw new IllegalStateException(
                    "Cannot register device for inactive user"
            );
        }


        // Only support the algorithm used by TapPay
        if (!"EC".equalsIgnoreCase(
                request.keyAlgorithm()
        )) {

            throw new IllegalArgumentException(
                    "Unsupported key algorithm. TapPay requires EC"
            );
        }


        // Create device
        Device device = new Device();

        device.setUser(user);

        device.setDeviceIdentifier(
                request.deviceIdentifier().trim()
        );

        device.setPublicKey(
                request.publicKey().trim()
        );

        device.setKeyAlgorithm(
                request.keyAlgorithm().trim()
        );

        device.setActive(true);

        LocalDateTime now = LocalDateTime.now();

        device.setRegisteredAt(now);
        device.setLastSeenAt(now);


        // Save
        Device savedDevice =
                deviceRepository.save(device);

        return mapToResponse(savedDevice);
    }


    // =========================
    // GET DEVICE
    // =========================

    @Override
    @Transactional(readOnly = true)
    public DeviceResponse getDevice(UUID deviceId) {

        if (deviceId == null) {
            throw new IllegalArgumentException(
                    "Device ID is required"
            );
        }

        Device device = deviceRepository
                .findById(deviceId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Device not found"
                        )
                );

        return mapToResponse(device);
    }


    // =========================
    // GET ACTIVE DEVICE
    // =========================

    @Override
    @Transactional(readOnly = true)
    public DeviceResponse getActiveDevice(
            UUID deviceId
    ) {

        if (deviceId == null) {
            throw new IllegalArgumentException(
                    "Device ID is required"
            );
        }

        Device device = deviceRepository
                .findByIdAndActiveTrue(deviceId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Active device not found"
                        )
                );

        return mapToResponse(device);
    }


    // =========================
    // DEACTIVATE DEVICE
    // =========================

    @Override
    @Transactional
    public void deactivateDevice(UUID deviceId) {

        if (deviceId == null) {
            throw new IllegalArgumentException(
                    "Device ID is required"
            );
        }

        Device device = deviceRepository
                .findById(deviceId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Device not found"
                        )
                );

        if (!device.isActive()) {
            return;
        }

        device.setActive(false);

        deviceRepository.save(device);
    }


    // =========================
    // UPDATE LAST SEEN
    // =========================

    @Override
    @Transactional
    public void updateLastSeen(UUID deviceId) {

        if (deviceId == null) {
            throw new IllegalArgumentException(
                    "Device ID is required"
            );
        }

        Device device = deviceRepository
                .findById(deviceId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Device not found"
                        )
                );

        device.setLastSeenAt(
                LocalDateTime.now()
        );

        deviceRepository.save(device);
    }


    // =========================
    // ENTITY → RESPONSE
    // =========================

    private DeviceResponse mapToResponse(
            Device device
    ) {

        return new DeviceResponse(
                device.getId(),
                device.getUser().getId(),
                device.isActive(),
                device.getRegisteredAt(),
                device.getLastSeenAt()
        );
    }
}