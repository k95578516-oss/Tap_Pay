package com.example.tap_pay;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "settlements")
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", unique = true)
    private Transaction transaction;

    private UUID senderWalletId;

    private UUID receiverWalletId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    private LocalDateTime settledAt;

    @Enumerated(EnumType.STRING)
    private SettlementStatus status;

    private String settlementReference;

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setStatus(SettlementStatus status) {
        this.status = status;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setSettledAt(LocalDateTime settledAt) {
        this.settledAt = settledAt;
    }

    public SettlementStatus getStatus() {
        return status;
    }

    public LocalDateTime getSettledAt() {
        return settledAt;
    }

    public String getSettlementReference() {
        return settlementReference;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public UUID getReceiverWalletId() {
        return receiverWalletId;
    }

    public UUID getSenderWalletId() {
        return senderWalletId;
    }

    public void setReceiverWalletId(UUID receiverWalletId) {
        this.receiverWalletId = receiverWalletId;
    }

    public void setSenderWalletId(UUID senderWalletId) {
        this.senderWalletId = senderWalletId;
    }

    public void setSettlementReference(String settlementReference) {
        this.settlementReference = settlementReference;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
}
