package com.example.tap_pay;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }


    // =========================
    // REGISTER DEVICE
    // =========================

    @PostMapping("/user/{userId}/register")
    public ResponseEntity<DeviceResponse> registerDevice(
            @PathVariable UUID userId,
            @RequestBody RegisterDeviceRequest request
    ) {

        DeviceResponse response =
                deviceService.registerDevice(
                        userId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // =========================
    // GET DEVICE
    // =========================

    @GetMapping("/{deviceId}")
    public ResponseEntity<DeviceResponse> getDevice(
            @PathVariable UUID deviceId
    ) {

        DeviceResponse response =
                deviceService.getDevice(deviceId);

        return ResponseEntity.ok(response);
    }


    // =========================
    // GET ACTIVE DEVICE
    // =========================

    @GetMapping("/{deviceId}/active")
    public ResponseEntity<DeviceResponse> getActiveDevice(
            @PathVariable UUID deviceId
    ) {

        DeviceResponse response =
                deviceService.getActiveDevice(deviceId);

        return ResponseEntity.ok(response);
    }


    // =========================
    // DEACTIVATE DEVICE
    // =========================

    @PutMapping("/{deviceId}/deactivate")
    public ResponseEntity<Void> deactivateDevice(
            @PathVariable UUID deviceId
    ) {

        deviceService.deactivateDevice(deviceId);

        return ResponseEntity.noContent().build();
    }
}