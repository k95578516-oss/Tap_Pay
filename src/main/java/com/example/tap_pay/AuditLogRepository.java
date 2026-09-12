package com.example.tap_pay;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findByUserId(UUID userId);

    List<AuditLog> findByTransactionId(UUID transactionId);

    List<AuditLog> findByAction(String action);
}