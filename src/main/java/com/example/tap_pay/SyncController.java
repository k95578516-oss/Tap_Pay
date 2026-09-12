package com.example.tap_pay;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping("/transactions")
    public ResponseEntity<SyncTransactionsResponse> syncTransactions(
            @RequestBody SyncTransactionsRequest request
    ) {

        SyncTransactionsResponse response =
                syncService.syncTransactions(request);

        return ResponseEntity.ok(response);
    }
}