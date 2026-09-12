package com.example.tap_pay;

import java.util.List;
import java.util.UUID;

public record SyncTransactionsResponse(

        UUID batchId,

        List<TransactionSyncResult> results

) {
    @Override
    public List<TransactionSyncResult> results() {
        return results;

    }

    @Override
    public UUID batchId() {
        return batchId;
    }

}