package com.example.tap_pay;

import java.util.List;
import java.util.UUID;

public record SyncTransactionsRequest(

        UUID deviceId,

        List<TransactionSyncItem> transactions

) {
    @Override
    public List<TransactionSyncItem> transactions() {
        return transactions;
    }

    @Override
    public UUID deviceId() {
        return deviceId;
    }
}