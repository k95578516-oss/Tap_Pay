package com.example.tap_pay;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private UserRepository userRepository;

    private DeviceServiceImpl deviceService;

    private UUID userId;
    private UUID deviceId;

    private User user;
    private Device device;

    @BeforeEach
    void setUp() {

        deviceService = new DeviceServiceImpl(
                deviceRepository,
                userRepository
        );

        userId = UUID.randomUUID();
        deviceId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setActive(true);

        device = new Device();
        device.setId(deviceId);
        device.setUser(user);
        device.setDeviceIdentifier("DEVICE-001");
        device.setPublicKey("PUBLIC-KEY");
        device.setKeyAlgorithm("EC");
        device.setActive(true);
        device.setRegisteredAt(LocalDateTime.now().minusMinutes(10));
        device.setLastSeenAt(LocalDateTime.now().minusMinutes(5));
    }


    // =========================================================
    // REGISTER DEVICE
    // =========================================================

    @Test
    void registerDevice_shouldRegisterSuccessfully() {

        RegisterDeviceRequest request =
                new RegisterDeviceRequest(
                        "DEVICE-001",
                        "PUBLIC-KEY",
                        "EC"
                );

        when(deviceRepository
                .findByDeviceIdentifier("DEVICE-001"))
                .thenReturn(Optional.empty());

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(deviceRepository.save(any(Device.class)))
                .thenAnswer(invocation -> {

                    Device saved =
                            invocation.getArgument(0);

                    saved.setId(deviceId);

                    return saved;
                });

        DeviceResponse response =
                deviceService.registerDevice(
                        userId,
                        request
                );

        assertNotNull(response);

        assertEquals(deviceId, response.deviceId());
        assertEquals(userId, response.userId());
        assertTrue(response.active());

        assertNotNull(response.registeredAt());
        assertNotNull(response.lastSeenAt());

        verify(deviceRepository)
                .findByDeviceIdentifier("DEVICE-001");

        verify(userRepository)
                .findById(userId);

        verify(deviceRepository)
                .save(any(Device.class));
    }


    @Test
    void registerDevice_shouldTrimDeviceDataBeforeSaving() {

        RegisterDeviceRequest request =
                new RegisterDeviceRequest(
                        "  DEVICE-001  ",
                        "  PUBLIC-KEY  ",
                        "EC"
                );

        when(deviceRepository
                .findByDeviceIdentifier("  DEVICE-001  "))
                .thenReturn(Optional.empty());

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        ArgumentCaptor<Device> captor =
                ArgumentCaptor.forClass(Device.class);

        when(deviceRepository.save(captor.capture()))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        deviceService.registerDevice(
                userId,
                request
        );

        Device savedDevice =
                captor.getValue();

        assertEquals(
                "DEVICE-001",
                savedDevice.getDeviceIdentifier()
        );

        assertEquals(
                "PUBLIC-KEY",
                savedDevice.getPublicKey()
        );

        assertEquals(
                "EC",
                savedDevice.getKeyAlgorithm()
        );

        assertTrue(savedDevice.isActive());

        assertNotNull(savedDevice.getRegisteredAt());
        assertNotNull(savedDevice.getLastSeenAt());
    }


    @Test
    void registerDevice_shouldRejectNullUserId() {

        RegisterDeviceRequest request =
                new RegisterDeviceRequest(
                        "DEVICE-001",
                        "PUBLIC-KEY",
                        "EC"
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> deviceService.registerDevice(
                                null,
                                request
                        )
                );

        assertEquals(
                "User ID is required",
                exception.getMessage()
        );

        verifyNoInteractions(
                deviceRepository,
                userRepository
        );
    }


    @Test
    void registerDevice_shouldRejectNullRequest() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> deviceService.registerDevice(
                                userId,
                                null
                        )
                );

        assertEquals(
                "Device registration request is required",
                exception.getMessage()
        );

        verifyNoInteractions(
                deviceRepository,
                userRepository
        );
    }


    @Test
    void registerDevice_shouldRejectBlankDeviceIdentifier() {

        RegisterDeviceRequest request =
                new RegisterDeviceRequest(
                        "   ",
                        "PUBLIC-KEY",
                        "EC"
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> deviceService.registerDevice(
                                userId,
                                request
                        )
                );

        assertEquals(
                "Device identifier is required",
                exception.getMessage()
        );

        verifyNoInteractions(
                deviceRepository,
                userRepository
        );
    }


    @Test
    void registerDevice_shouldRejectBlankPublicKey() {

        RegisterDeviceRequest request =
                new RegisterDeviceRequest(
                        "DEVICE-001",
                        "   ",
                        "EC"
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> deviceService.registerDevice(
                                userId,
                                request
                        )
                );

        assertEquals(
                "Public key is required",
                exception.getMessage()
        );

        verifyNoInteractions(
                deviceRepository,
                userRepository
        );
    }


    @Test
    void registerDevice_shouldRejectBlankKeyAlgorithm() {

        RegisterDeviceRequest request =
                new RegisterDeviceRequest(
                        "DEVICE-001",
                        "PUBLIC-KEY",
                        "   "
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> deviceService.registerDevice(
                                userId,
                                request
                        )
                );

        assertEquals(
                "Key algorithm is required",
                exception.getMessage()
        );

        verifyNoInteractions(
                deviceRepository,
                userRepository
        );
    }


    @Test
    void registerDevice_shouldRejectDuplicateDevice() {

        RegisterDeviceRequest request =
                new RegisterDeviceRequest(
                        "DEVICE-001",
                        "PUBLIC-KEY",
                        "EC"
                );

        when(deviceRepository
                .findByDeviceIdentifier("DEVICE-001"))
                .thenReturn(Optional.of(device));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> deviceService.registerDevice(
                                userId,
                                request
                        )
                );

        assertEquals(
                "Device is already registered",
                exception.getMessage()
        );

        verify(deviceRepository)
                .findByDeviceIdentifier("DEVICE-001");

        verify(userRepository, never())
                .findById(any());

        verify(deviceRepository, never())
                .save(any());
    }


    @Test
    void registerDevice_shouldRejectWhenUserNotFound() {

        RegisterDeviceRequest request =
                new RegisterDeviceRequest(
                        "DEVICE-001",
                        "PUBLIC-KEY",
                        "EC"
                );

        when(deviceRepository
                .findByDeviceIdentifier("DEVICE-001"))
                .thenReturn(Optional.empty());

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> deviceService.registerDevice(
                                userId,
                                request
                        )
                );

        assertEquals(
                "User not found",
                exception.getMessage()
        );

        verify(deviceRepository)
                .findByDeviceIdentifier("DEVICE-001");

        verify(userRepository)
                .findById(userId);

        verify(deviceRepository, never())
                .save(any());
    }


    @Test
    void registerDevice_shouldRejectInactiveUser() {

        user.setActive(false);

        RegisterDeviceRequest request =
                new RegisterDeviceRequest(
                        "DEVICE-001",
                        "PUBLIC-KEY",
                        "EC"
                );

        when(deviceRepository
                .findByDeviceIdentifier("DEVICE-001"))
                .thenReturn(Optional.empty());

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> deviceService.registerDevice(
                                userId,
                                request
                        )
                );

        assertEquals(
                "Cannot register device for inactive user",
                exception.getMessage()
        );

        verify(deviceRepository, never())
                .save(any());
    }


    @Test
    void registerDevice_shouldRejectUnsupportedKeyAlgorithm() {

        RegisterDeviceRequest request =
                new RegisterDeviceRequest(
                        "DEVICE-001",
                        "PUBLIC-KEY",
                        "RSA"
                );

        when(deviceRepository
                .findByDeviceIdentifier("DEVICE-001"))
                .thenReturn(Optional.empty());

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> deviceService.registerDevice(
                                userId,
                                request
                        )
                );

        assertEquals(
                "Unsupported key algorithm. TapPay requires EC",
                exception.getMessage()
        );

        verify(deviceRepository, never())
                .save(any());
    }


    @Test
    void registerDevice_shouldAcceptLowercaseEcAlgorithm() {

        RegisterDeviceRequest request =
                new RegisterDeviceRequest(
                        "DEVICE-001",
                        "PUBLIC-KEY",
                        "ec"
                );

        when(deviceRepository
                .findByDeviceIdentifier("DEVICE-001"))
                .thenReturn(Optional.empty());

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        ArgumentCaptor<Device> captor =
                ArgumentCaptor.forClass(Device.class);

        when(deviceRepository.save(captor.capture()))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        deviceService.registerDevice(
                userId,
                request
        );

        Device savedDevice =
                captor.getValue();

        assertEquals(
                "ec",
                savedDevice.getKeyAlgorithm()
        );

        assertTrue(savedDevice.isActive());
    }


    // =========================================================
    // GET DEVICE
    // =========================================================

    @Test
    void getDevice_shouldReturnDeviceSuccessfully() {

        when(deviceRepository.findById(deviceId))
                .thenReturn(Optional.of(device));

        DeviceResponse response =
                deviceService.getDevice(deviceId);

        assertNotNull(response);

        assertEquals(
                deviceId,
                response.deviceId()
        );

        assertEquals(
                userId,
                response.userId()
        );

        assertTrue(response.active());

        assertEquals(
                device.getRegisteredAt(),
                response.registeredAt()
        );

        assertEquals(
                device.getLastSeenAt(),
                response.lastSeenAt()
        );

        verify(deviceRepository)
                .findById(deviceId);
    }


    @Test
    void getDevice_shouldRejectNullDeviceId() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> deviceService.getDevice(null)
                );

        assertEquals(
                "Device ID is required",
                exception.getMessage()
        );

        verifyNoInteractions(deviceRepository);
    }


    @Test
    void getDevice_shouldRejectWhenDeviceNotFound() {

        when(deviceRepository.findById(deviceId))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> deviceService.getDevice(deviceId)
                );

        assertEquals(
                "Device not found",
                exception.getMessage()
        );

        verify(deviceRepository)
                .findById(deviceId);
    }


    // =========================================================
    // GET ACTIVE DEVICE
    // =========================================================

    @Test
    void getActiveDevice_shouldReturnActiveDeviceSuccessfully() {

        when(deviceRepository
                .findByIdAndActiveTrue(deviceId))
                .thenReturn(Optional.of(device));

        DeviceResponse response =
                deviceService.getActiveDevice(deviceId);

        assertNotNull(response);

        assertEquals(
                deviceId,
                response.deviceId()
        );

        assertEquals(
                userId,
                response.userId()
        );

        assertTrue(response.active());

        verify(deviceRepository)
                .findByIdAndActiveTrue(deviceId);
    }


    @Test
    void getActiveDevice_shouldRejectNullDeviceId() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> deviceService.getActiveDevice(null)
                );

        assertEquals(
                "Device ID is required",
                exception.getMessage()
        );

        verifyNoInteractions(deviceRepository);
    }


    @Test
    void getActiveDevice_shouldRejectWhenDeviceNotFound() {

        when(deviceRepository
                .findByIdAndActiveTrue(deviceId))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> deviceService.getActiveDevice(deviceId)
                );

        assertEquals(
                "Active device not found",
                exception.getMessage()
        );

        verify(deviceRepository)
                .findByIdAndActiveTrue(deviceId);
    }


    // =========================================================
    // DEACTIVATE DEVICE
    // =========================================================

    @Test
    void deactivateDevice_shouldDeactivateActiveDevice() {

        device.setActive(true);

        when(deviceRepository.findById(deviceId))
                .thenReturn(Optional.of(device));

        deviceService.deactivateDevice(deviceId);

        assertFalse(device.isActive());

        verify(deviceRepository)
                .findById(deviceId);

        verify(deviceRepository)
                .save(device);
    }


    @Test
    void deactivateDevice_shouldDoNothingIfAlreadyInactive() {

        device.setActive(false);

        when(deviceRepository.findById(deviceId))
                .thenReturn(Optional.of(device));

        deviceService.deactivateDevice(deviceId);

        assertFalse(device.isActive());

        verify(deviceRepository)
                .findById(deviceId);

        verify(deviceRepository, never())
                .save(any());
    }


    @Test
    void deactivateDevice_shouldRejectNullDeviceId() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> deviceService.deactivateDevice(null)
                );

        assertEquals(
                "Device ID is required",
                exception.getMessage()
        );

        verifyNoInteractions(deviceRepository);
    }


    @Test
    void deactivateDevice_shouldRejectWhenDeviceNotFound() {

        when(deviceRepository.findById(deviceId))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> deviceService.deactivateDevice(deviceId)
                );

        assertEquals(
                "Device not found",
                exception.getMessage()
        );

        verify(deviceRepository)
                .findById(deviceId);

        verify(deviceRepository, never())
                .save(any());
    }


    // =========================================================
    // UPDATE LAST SEEN
    // =========================================================

    @Test
    void updateLastSeen_shouldUpdateTimestamp() {

        LocalDateTime oldLastSeen =
                device.getLastSeenAt();

        when(deviceRepository.findById(deviceId))
                .thenReturn(Optional.of(device));

        deviceService.updateLastSeen(deviceId);

        assertNotNull(device.getLastSeenAt());

        assertTrue(
                device.getLastSeenAt()
                        .isAfter(oldLastSeen)
                        || device.getLastSeenAt()
                        .isEqual(oldLastSeen)
        );

        verify(deviceRepository)
                .findById(deviceId);

        verify(deviceRepository)
                .save(device);
    }


    @Test
    void updateLastSeen_shouldRejectNullDeviceId() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> deviceService.updateLastSeen(null)
                );

        assertEquals(
                "Device ID is required",
                exception.getMessage()
        );

        verifyNoInteractions(deviceRepository);
    }


    @Test
    void updateLastSeen_shouldRejectWhenDeviceNotFound() {

        when(deviceRepository.findById(deviceId))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> deviceService.updateLastSeen(deviceId)
                );

        assertEquals(
                "Device not found",
                exception.getMessage()
        );

        verify(deviceRepository)
                .findById(deviceId);

        verify(deviceRepository, never())
                .save(any());
    }
}