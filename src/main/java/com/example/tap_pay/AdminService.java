package com.example.tap_pay;

import java.util.List;
import java.util.UUID;

public interface AdminService {

    List<User> getAllUsers();

    User getUser(UUID userId);

    void deactivateUser(UUID userId);

    void activateUser(UUID userId);

    List<Device> getAllDevices();

    void deactivateDevice(UUID deviceId);

    List<Transaction> getAllTransactions();

    List<Transaction> getTransactionsByStatus(TransactionStatus status);

    List<AuditLog> getAllAuditLogs();

    List<AuditLog> getUserAuditLogs(UUID userId);

    List<AuditLog> getTransactionAuditLogs(UUID transactionId);

    long getTotalUsers();

    long getTotalDevices();

    long getTotalTransactions();

    long getSettledTransactions();

    long getRejectedTransactions();
}