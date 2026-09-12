package com.example.tap_pay;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(

        UUID txId,

        UUID senderId,

        UUID receiverId,

        BigDecimal amount,

        String currency,

        TransactionStatus status,

        LocalDateTime receivedAt,

        LocalDateTime settledAt,

        RejectionReason rejectionReason

) {
    @Override
    public UUID senderId() {
        return senderId;
    }

    @Override
    public UUID receiverId() {
        return receiverId;
    }

    @Override
    public UUID txId() {
        return txId;
    }

    @Override
    public String currency() {
        return currency;
    }

    @Override
    public BigDecimal amount() {
        return amount;
    }

    @Override
    public LocalDateTime receivedAt() {
        return receivedAt;
    }

    @Override
    public LocalDateTime settledAt() {
        return settledAt;
    }

    @Override
    public RejectionReason rejectionReason() {
        return rejectionReason;
    }

    @Override
    public TransactionStatus status() {
        return status;
    }
}