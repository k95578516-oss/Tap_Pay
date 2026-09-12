package com.example.tap_pay;

import java.util.List;
import java.util.UUID;

public interface AuditService {

    AuditLog log(
            UUID userId,
            UUID transactionId,
            String action,
            String details
    );

    List<AuditLog> getUserLogs(UUID userId);

    List<AuditLog> getTransactionLogs(UUID transactionId);

    List<AuditLog> getLogsByAction(String action);
}