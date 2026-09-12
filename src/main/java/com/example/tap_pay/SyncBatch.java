package com.example.tap_pay;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sync_batches")
public class SyncBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private Device device;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private Integer transactionCount;

    private Integer successCount;

    private Integer rejectedCount;

    @Enumerated(EnumType.STRING)
    private SyncBatchStatus status;

    public void setStatus(SyncBatchStatus status) {
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getRejectedCount() {
        return rejectedCount;
    }

    public Device getDevice() {
        return device;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public Integer getTransactionCount() {
        return transactionCount;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setRejectedCount(Integer rejectedCount) {
        this.rejectedCount = rejectedCount;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public SyncBatchStatus getStatus() {
        return status;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public void setTransactionCount(Integer transactionCount) {
        this.transactionCount = transactionCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }
}