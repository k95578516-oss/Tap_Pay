package com.example.tap_pay;

import org.springframework.stereotype.Service;

@Service
public class NonceServiceImpl implements NonceService {

    private final TransactionRepository transactionRepository;

    public NonceServiceImpl(
            TransactionRepository transactionRepository
    ) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public boolean isNonceUsed(String nonce) {

        if (nonce == null || nonce.isBlank()) {
            return false;
        }

        return transactionRepository.existsByNonce(nonce);
    }

    @Override
    public void validateNonce(String nonce) {

        // Nonce must exist
        if (nonce == null || nonce.isBlank()) {
            throw new RuntimeException(
                    "Nonce is required"
            );
        }

        // Nonce must not have been used before
        if (transactionRepository.existsByNonce(nonce)) {
            throw new RuntimeException(
                    "Nonce already used"
            );
        }
    }
}