package com.example.tap_pay;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "transactions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tx_nonce", columnNames = "nonce")
        }
)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_device_id", nullable = false)
    private Device senderDevice;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, unique = true)
    private String nonce;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String signature;

    @Column(nullable = false)
    private Long clientTimestamp;

    private LocalDateTime receivedAt;

    private LocalDateTime settledAt;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Enumerated(EnumType.STRING)
    private RejectionReason rejectionReason;

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public Device getSenderDevice() {
        return senderDevice;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public LocalDateTime getSettledAt() {
        return settledAt;
    }

    public Long getClientTimestamp() {
        return clientTimestamp;
    }

    public String getCurrency() {
        return currency;
    }

    public RejectionReason getRejectionReason() {
        return rejectionReason;
    }

    public String getNonce() {
        return nonce;
    }

    public String getSignature() {
        return signature;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public User getReceiver() {
        return receiver;
    }

    public User getSender() {
        return sender;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setClientTimestamp(Long clientTimestamp) {
        this.clientTimestamp = clientTimestamp;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public void setRejectionReason(RejectionReason rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public void setSenderDevice(Device senderDevice) {
        this.senderDevice = senderDevice;
    }

    public void setSettledAt(LocalDateTime settledAt) {
        this.settledAt = settledAt;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
}
