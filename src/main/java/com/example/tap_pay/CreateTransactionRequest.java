package com.example.tap_pay;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateTransactionRequest(

        UUID txId,

        UUID senderId,

        UUID receiverId,

        UUID senderDeviceId,

        BigDecimal amount,

        String currency,

        String nonce,

        Long timestamp,

        String signature
) {
    @Override
    public BigDecimal amount() {
        return amount;
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
    public UUID receiverId() {
        return receiverId;
    }

    @Override
    public UUID senderId() {
        return senderId;
    }

    @Override
    public String signature() {
        return signature;
    }

    @Override
    public String nonce() {
        return nonce;
    }

    @Override
    public Long timestamp() {
        return timestamp;
    }

    @Override
    public UUID senderDeviceId() {
        return senderDeviceId;
    }

}