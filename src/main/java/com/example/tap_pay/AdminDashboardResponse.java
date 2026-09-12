package com.example.tap_pay;

public record AdminDashboardResponse(
        long totalUsers,
        long totalDevices,
        long totalTransactions,
        long settledTransactions,
        long rejectedTransactions
) {
}