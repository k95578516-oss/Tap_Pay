package com.example.tap_pay;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class SignatureServiceImpl implements SignatureService {

    private static final String SIGNATURE_ALGORITHM =
            "SHA256withECDSA";

    private static final String KEY_ALGORITHM =
            "EC";

    @Override
    public boolean verifyTransactionSignature(
            Transaction transaction
    ) {

        try {

            // Get public key stored during device registration
            String publicKeyString =
                    transaction
                            .getSenderDevice()
                            .getPublicKey();

            if (publicKeyString == null ||
                    publicKeyString.isBlank()) {

                return false;
            }

            // Convert Base64 public key into PublicKey object
            PublicKey publicKey =
                    decodePublicKey(publicKeyString);

            // Reconstruct the EXACT payload that was signed
            String payload =
                    buildPayload(transaction);

            // Decode Base64 signature
            byte[] signatureBytes =
                    Base64.getDecoder().decode(
                            transaction.getSignature()
                    );

            // Create ECDSA verifier
            Signature verifier =
                    Signature.getInstance(
                            SIGNATURE_ALGORITHM
                    );

            verifier.initVerify(publicKey);

            // Give verifier the original signed data
            verifier.update(
                    payload.getBytes(StandardCharsets.UTF_8)
            );

            // Verify signature
            return verifier.verify(signatureBytes);

        } catch (Exception e) {

            return false;
        }
    }

    private String buildPayload(
            Transaction transaction
    ) {

        return transaction.getId()
                + "|"
                + transaction.getSender().getId()
                + "|"
                + transaction.getReceiver().getId()
                + "|"
                + transaction.getAmount()
                + "|"
                + transaction.getCurrency()
                + "|"
                + transaction.getNonce()
                + "|"
                + transaction.getClientTimestamp();
    }

    private PublicKey decodePublicKey(
            String publicKeyString
    ) throws Exception {

        byte[] keyBytes =
                Base64.getDecoder().decode(
                        publicKeyString
                );

        X509EncodedKeySpec keySpec =
                new X509EncodedKeySpec(keyBytes);

        KeyFactory keyFactory =
                KeyFactory.getInstance(KEY_ALGORITHM);

        return keyFactory.generatePublic(keySpec);
    }
}
