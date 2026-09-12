package com.example.tap_pay;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionSyncItem(

        UUID txId,

        UUID senderId,

        UUID receiverId,

        BigDecimal amount,

        String currency,

        String nonce,

        Long timestamp,

        String signature

) {
    public UUID senderDeviceId() {
        return senderId;
    }

    @Override
    public BigDecimal amount() {
        return amount;
    }

    @Override
    public Long timestamp() {
        return timestamp;
    }

    @Override
    public String currency() {
        return currency;
    }

    @Override
    public String nonce() {
        return nonce;
    }

    @Override
    public UUID receiverId() {
        return receiverId;
    }

    @Override
    public String signature() {
        return signature;
    }

    @Override
    public UUID txId() {
        return txId;
    }

    @Override
    public UUID senderId() {
        return senderId;
    }

}