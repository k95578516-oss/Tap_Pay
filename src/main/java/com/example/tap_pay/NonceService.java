package com.example.tap_pay;

public interface NonceService {

    boolean isNonceUsed(String nonce);

    void validateNonce(String nonce);
}
