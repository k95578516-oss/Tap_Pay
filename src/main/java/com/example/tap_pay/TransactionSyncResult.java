package com.example.tap_pay;

import java.util.UUID;

public record TransactionSyncResult(

        UUID txId,

        TransactionStatus status,

        RejectionReason rejectionReason,

        String settlementReference

) {}