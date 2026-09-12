package com.example.tap_pay;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletResponse(

        UUID userId,

        BigDecimal availableBalance,

        BigDecimal offlineLimit,

        BigDecimal offlineUsed



) {
    @Override
    public BigDecimal availableBalance() {
        return availableBalance;
    }

    @Override
    public BigDecimal offlineLimit() {
        return offlineLimit;
    }

    @Override
    public BigDecimal offlineUsed() {
        return offlineUsed;
    }

    @Override
    public UUID userId() {
        return userId;
    }

}
