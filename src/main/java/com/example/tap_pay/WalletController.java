package com.example.tap_pay;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<WalletResponse> createWallet(
            @PathVariable UUID userId
    ) {

        WalletResponse response =
                walletService.createWallet(userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<WalletResponse> getWallet(
            @PathVariable UUID userId
    ) {

        WalletResponse response =
                walletService.getWallet(userId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/{userId}/balance/add")
    public ResponseEntity<WalletResponse> addBalance(
            @PathVariable UUID userId,
            @RequestParam BigDecimal amount
    ) {

        WalletResponse response =
                walletService.addBalance(userId, amount);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/{userId}/balance/deduct")
    public ResponseEntity<WalletResponse> deductBalance(
            @PathVariable UUID userId,
            @RequestParam BigDecimal amount
    ) {

        WalletResponse response =
                walletService.deductBalance(userId, amount);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/{userId}/offline/reserve")
    public ResponseEntity<WalletResponse> reserveOfflineAmount(
            @PathVariable UUID userId,
            @RequestParam BigDecimal amount
    ) {

        WalletResponse response =
                walletService.reserveOfflineAmount(userId, amount);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/{userId}/offline/release")
    public ResponseEntity<WalletResponse> releaseOfflineAmount(
            @PathVariable UUID userId,
            @RequestParam BigDecimal amount
    ) {

        WalletResponse response =
                walletService.releaseOfflineAmount(userId, amount);

        return ResponseEntity.ok(response);
    }
}