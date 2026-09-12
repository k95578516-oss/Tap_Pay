package com.example.tap_pay;

import java.math.BigDecimal;
import java.util.UUID;

public interface WalletService {

    WalletResponse createWallet(UUID userId);

    WalletResponse getWallet(UUID userId);

    WalletResponse addBalance(UUID userId, BigDecimal amount);

    WalletResponse deductBalance(UUID userId, BigDecimal amount);

    WalletResponse reserveOfflineAmount(UUID userId, BigDecimal amount);

    WalletResponse releaseOfflineAmount(UUID userId, BigDecimal amount);
}