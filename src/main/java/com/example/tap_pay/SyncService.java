package com.example.tap_pay;

public interface SyncService {

    SyncTransactionsResponse syncTransactions(
            SyncTransactionsRequest request
    );
}