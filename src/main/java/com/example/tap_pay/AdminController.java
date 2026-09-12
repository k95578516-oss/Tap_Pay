package com.example.tap_pay;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // =========================
    // USER MANAGEMENT
    // =========================

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUser(
            @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(adminService.getUser(userId));
    }

    @PutMapping("/users/{userId}/deactivate")
    public ResponseEntity<Void> deactivateUser(
            @PathVariable UUID userId
    ) {
        adminService.deactivateUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{userId}/activate")
    public ResponseEntity<Void> activateUser(
            @PathVariable UUID userId
    ) {
        adminService.activateUser(userId);
        return ResponseEntity.noContent().build();
    }


    // =========================
    // DEVICE MANAGEMENT
    // =========================

    @GetMapping("/devices")
    public ResponseEntity<List<Device>> getAllDevices() {
        return ResponseEntity.ok(adminService.getAllDevices());
    }

    @PutMapping("/devices/{deviceId}/deactivate")
    public ResponseEntity<Void> deactivateDevice(
            @PathVariable UUID deviceId
    ) {
        adminService.deactivateDevice(deviceId);
        return ResponseEntity.noContent().build();
    }


    // =========================
    // TRANSACTION MANAGEMENT
    // =========================

    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(adminService.getAllTransactions());
    }

    @GetMapping("/transactions/status/{status}")
    public ResponseEntity<List<Transaction>> getTransactionsByStatus(
            @PathVariable TransactionStatus status
    ) {
        return ResponseEntity.ok(
                adminService.getTransactionsByStatus(status)
        );
    }


    // =========================
    // AUDIT LOG MANAGEMENT
    // =========================

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAllAuditLogs() {
        return ResponseEntity.ok(adminService.getAllAuditLogs());
    }

    @GetMapping("/audit-logs/user/{userId}")
    public ResponseEntity<List<AuditLog>> getUserAuditLogs(
            @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(
                adminService.getUserAuditLogs(userId)
        );
    }

    @GetMapping("/audit-logs/transaction/{transactionId}")
    public ResponseEntity<List<AuditLog>> getTransactionAuditLogs(
            @PathVariable UUID transactionId
    ) {
        return ResponseEntity.ok(
                adminService.getTransactionAuditLogs(transactionId)
        );
    }


    // =========================
    // ADMIN DASHBOARD
    // =========================

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {

        AdminDashboardResponse response =
                new AdminDashboardResponse(
                        adminService.getTotalUsers(),
                        adminService.getTotalDevices(),
                        adminService.getTotalTransactions(),
                        adminService.getSettledTransactions(),
                        adminService.getRejectedTransactions()
                );

        return ResponseEntity.ok(response);
    }
}