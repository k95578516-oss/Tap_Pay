package com.example.tap_pay;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final TransactionRepository transactionRepository;
    private final AuditLogRepository auditLogRepository;

    public AdminServiceImpl(
            UserRepository userRepository,
            DeviceRepository deviceRepository,
            TransactionRepository transactionRepository,
            AuditLogRepository auditLogRepository
    ) {
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
        this.transactionRepository = transactionRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public User getUser(UUID userId) {

        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );
    }

    @Override
    @Transactional
    public void deactivateUser(UUID userId) {

        User user = getUser(userId);

        user.setActive(false);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void activateUser(UUID userId) {

        User user = getUser(userId);

        user.setActive(true);

        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    @Override
    @Transactional
    public void deactivateDevice(UUID deviceId) {

        if (deviceId == null) {
            throw new IllegalArgumentException("Device ID is required");
        }

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() ->
                        new RuntimeException("Device not found")
                );

        device.setActive(false);

        deviceRepository.save(device);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByStatus(
            TransactionStatus status
    ) {

        if (status == null) {
            throw new IllegalArgumentException("Transaction status is required");
        }

        return transactionRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getUserAuditLogs(UUID userId) {

        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        return auditLogRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getTransactionAuditLogs(
            UUID transactionId
    ) {

        if (transactionId == null) {
            throw new IllegalArgumentException(
                    "Transaction ID is required"
            );
        }

        return auditLogRepository.findByTransactionId(transactionId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalUsers() {
        return userRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalDevices() {
        return deviceRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalTransactions() {
        return transactionRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long getSettledTransactions() {
        return transactionRepository
                .findByStatus(TransactionStatus.SETTLED)
                .size();
    }

    @Override
    @Transactional(readOnly = true)
    public long getRejectedTransactions() {
        return transactionRepository
                .findByStatus(TransactionStatus.REJECTED)
                .size();
    }
}