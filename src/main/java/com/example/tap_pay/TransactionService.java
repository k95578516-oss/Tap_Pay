package com.example.tap_pay;

import java.util.List;
import java.util.UUID;

public interface TransactionService {

    TransactionResponse createTransaction(
            CreateTransactionRequest request
    );

    TransactionResponse getTransaction(UUID transactionId);

    List<TransactionResponse> getSenderTransactions(
            UUID senderId
    );

    List<TransactionResponse> getReceiverTransactions(
            UUID receiverId
    );

    List<TransactionResponse> getTransactionsByStatus(
            TransactionStatus status
    );
}