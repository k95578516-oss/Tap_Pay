package com.example.tap_pay;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional
    public AuditLog log(
            UUID userId,
            UUID transactionId,
            String action,
            String details
    ) {

        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Audit action is required");
        }

        AuditLog auditLog = new AuditLog();

        auditLog.setUserId(userId);
        auditLog.setTransactionId(transactionId);
        auditLog.setAction(action);
        auditLog.setDetails(details);
        auditLog.setCreatedAt(LocalDateTime.now());

        return auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getUserLogs(UUID userId) {

        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        return auditLogRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getTransactionLogs(UUID transactionId) {

        if (transactionId == null) {
            throw new IllegalArgumentException("Transaction ID is required");
        }

        return auditLogRepository.findByTransactionId(transactionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getLogsByAction(String action) {

        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Audit action is required");
        }

        return auditLogRepository.findByAction(action);
    }
}