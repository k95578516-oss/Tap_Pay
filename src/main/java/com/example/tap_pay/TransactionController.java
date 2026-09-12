package com.example.tap_pay;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @RequestBody CreateTransactionRequest request
    ) {

        TransactionResponse response =
                transactionService.createTransaction(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransaction(
            @PathVariable UUID transactionId
    ) {

        TransactionResponse response =
                transactionService.getTransaction(transactionId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/sender/{senderId}")
    public ResponseEntity<List<TransactionResponse>> getSenderTransactions(
            @PathVariable UUID senderId
    ) {

        List<TransactionResponse> response =
                transactionService.getSenderTransactions(senderId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/receiver/{receiverId}")
    public ResponseEntity<List<TransactionResponse>> getReceiverTransactions(
            @PathVariable UUID receiverId
    ) {

        List<TransactionResponse> response =
                transactionService.getReceiverTransactions(receiverId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByStatus(
            @PathVariable TransactionStatus status
    ) {

        List<TransactionResponse> response =
                transactionService.getTransactionsByStatus(status);

        return ResponseEntity.ok(response);
    }
}