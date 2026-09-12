package com.example.tap_pay;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository
        extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByNonce(String nonce);

    boolean existsByNonce(String nonce);

    List<Transaction> findBySenderId(UUID senderId);

    List<Transaction> findByReceiverId(UUID receiverId);

    List<Transaction> findByStatus(
            TransactionStatus status
    );
}