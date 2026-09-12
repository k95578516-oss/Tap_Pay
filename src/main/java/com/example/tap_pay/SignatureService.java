package com.example.tap_pay;

public interface SignatureService {

    boolean verifyTransactionSignature(
            Transaction transaction
    );
}
