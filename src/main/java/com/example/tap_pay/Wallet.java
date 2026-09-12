package com.example.tap_pay;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal availableBalance;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal offlineLimit;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal offlineUsed;

    private LocalDateTime updatedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public BigDecimal getOfflineLimit() {
        return offlineLimit;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }

    public BigDecimal getOfflineUsed() {
        return offlineUsed;
    }

    public void setOfflineLimit(BigDecimal offlineLimit) {
        this.offlineLimit = offlineLimit;
    }

    public void setOfflineUsed(BigDecimal offlineUsed) {
        this.offlineUsed = offlineUsed;
    }
}