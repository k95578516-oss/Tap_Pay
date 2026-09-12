package com.example.tap_pay;

import java.util.UUID;

public interface SettlementService {

    Settlement settleTransaction(UUID transactionId);

    Settlement getSettlement(UUID transactionId);
}
