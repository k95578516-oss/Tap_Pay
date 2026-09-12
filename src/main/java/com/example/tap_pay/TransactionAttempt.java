package com.example.tap_pay;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transaction_attempts")
public class TransactionAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID transactionId;

    private UUID deviceId;

    private LocalDateTime attemptedAt;

    private String result;

    private String rejectionReason;

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public LocalDateTime getAttemptedAt() {
        return attemptedAt;
    }

    public String getResult() {
        return result;
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public void setAttemptedAt(LocalDateTime attemptedAt) {
        this.attemptedAt = attemptedAt;
    }

    public void setDeviceId(UUID deviceId) {
        this.deviceId = deviceId;
    }

    public void setResult(String result) {
        this.result = result;
    }
}