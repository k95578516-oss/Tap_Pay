package com.example.tap_pay;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SignatureServiceTest {

    private SignatureServiceImpl signatureService;

    private Transaction transaction;
    private User sender;
    private User receiver;
    private Device senderDevice;

    private KeyPair keyPair;

    private UUID transactionId;
    private UUID senderId;
    private UUID receiverId;
    private UUID deviceId;

    @BeforeEach
    void setUp() throws Exception {

        signatureService = new SignatureServiceImpl();

        transactionId = UUID.randomUUID();
        senderId = UUID.randomUUID();
        receiverId = UUID.randomUUID();
        deviceId = UUID.randomUUID();

        sender = new User();
        sender.setId(senderId);

        receiver = new User();
        receiver.setId(receiverId);

        KeyPairGenerator keyPairGenerator =
                KeyPairGenerator.getInstance("EC");

        keyPairGenerator.initialize(256);

        keyPair = keyPairGenerator.generateKeyPair();

        senderDevice = new Device();
        senderDevice.setId(deviceId);

        senderDevice.setUser(sender);

        senderDevice.setPublicKey(
                Base64.getEncoder().encodeToString(
                        keyPair.getPublic().getEncoded()
                )
        );

        senderDevice.setKeyAlgorithm("EC");
        senderDevice.setActive(true);

        transaction = new Transaction();

        transaction.setId(transactionId);
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setSenderDevice(senderDevice);
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setCurrency("INR");
        transaction.setNonce("nonce-12345");
        transaction.setClientTimestamp(
                System.currentTimeMillis()
        );

        transaction.setSignature(
                createValidSignature(transaction)
        );
    }

    @Test
    void verifyTransactionSignature_shouldReturnTrueForValidSignature()
            throws Exception {

        boolean result =
                signatureService.verifyTransactionSignature(
                        transaction
                );

        assertTrue(result);
    }

    @Test
    void verifyTransactionSignature_shouldReturnFalseForInvalidSignature()
            throws Exception {

        transaction.setSignature(
                Base64.getEncoder().encodeToString(
                        "invalid-signature".getBytes(
                                StandardCharsets.UTF_8
                        )
                )
        );

        boolean result =
                signatureService.verifyTransactionSignature(
                        transaction
                );

        assertFalse(result);
    }

    @Test
    void verifyTransactionSignature_shouldReturnFalseWhenPublicKeyIsMissing() {

        senderDevice.setPublicKey(null);

        boolean result =
                signatureService.verifyTransactionSignature(
                        transaction
                );

        assertFalse(result);
    }

    @Test
    void verifyTransactionSignature_shouldReturnFalseWhenPublicKeyIsBlank() {

        senderDevice.setPublicKey("   ");

        boolean result =
                signatureService.verifyTransactionSignature(
                        transaction
                );

        assertFalse(result);
    }

    @Test
    void verifyTransactionSignature_shouldReturnFalseForMalformedPublicKey() {

        senderDevice.setPublicKey("not-a-valid-base64-key");

        boolean result =
                signatureService.verifyTransactionSignature(
                        transaction
                );

        assertFalse(result);
    }

    @Test
    void verifyTransactionSignature_shouldReturnFalseWhenSignatureIsMissing() {

        transaction.setSignature(null);

        boolean result =
                signatureService.verifyTransactionSignature(
                        transaction
                );

        assertFalse(result);
    }

    @Test
    void verifyTransactionSignature_shouldReturnFalseWhenSignatureIsBlank() {

        transaction.setSignature("");

        boolean result =
                signatureService.verifyTransactionSignature(
                        transaction
                );

        assertFalse(result);
    }

    @Test
    void verifyTransactionSignature_shouldReturnFalseWhenAmountIsTampered()
            throws Exception {

        transaction.setAmount(new BigDecimal("999.00"));

        boolean result =
                signatureService.verifyTransactionSignature(
                        transaction
                );

        assertFalse(result);
    }

    @Test
    void verifyTransactionSignature_shouldReturnFalseWhenReceiverIsTampered()
            throws Exception {

        User anotherReceiver = new User();
        anotherReceiver.setId(UUID.randomUUID());

        transaction.setReceiver(anotherReceiver);

        boolean result =
                signatureService.verifyTransactionSignature(
                        transaction
                );

        assertFalse(result);
    }

    @Test
    void verifyTransactionSignature_shouldReturnFalseWhenNonceIsTampered()
            throws Exception {

        transaction.setNonce("tampered-nonce");

        boolean result =
                signatureService.verifyTransactionSignature(
                        transaction
                );

        assertFalse(result);
    }

    @Test
    void verifyTransactionSignature_shouldReturnFalseWhenCurrencyIsTampered()
            throws Exception {

        transaction.setCurrency("USD");

        boolean result =
                signatureService.verifyTransactionSignature(
                        transaction
                );

        assertFalse(result);
    }

    @Test
    void verifyTransactionSignature_shouldReturnFalseWhenTimestampIsTampered()
            throws Exception {

        transaction.setClientTimestamp(
                System.currentTimeMillis() + 1000
        );

        boolean result =
                signatureService.verifyTransactionSignature(
                        transaction
                );

        assertFalse(result);
    }

    @Test
    void verifyTransactionSignature_shouldReturnFalseWhenTransactionIdIsTampered()
            throws Exception {

        transaction.setId(UUID.randomUUID());

        boolean result =
                signatureService.verifyTransactionSignature(
                        transaction
                );

        assertFalse(result);
    }

    @Test
    void verifyTransactionSignature_shouldReturnFalseWhenSenderIsTampered()
            throws Exception {

        User anotherSender = new User();
        anotherSender.setId(UUID.randomUUID());

        transaction.setSender(anotherSender);

        boolean result =
                signatureService.verifyTransactionSignature(
                        transaction
                );

        assertFalse(result);
    }

    @Test
    void verifyTransactionSignature_shouldReturnFalseWhenPublicKeyBelongsToAnotherDevice()
            throws Exception {

        KeyPairGenerator keyPairGenerator =
                KeyPairGenerator.getInstance("EC");

        keyPairGenerator.initialize(256);

        KeyPair anotherKeyPair =
                keyPairGenerator.generateKeyPair();

        senderDevice.setPublicKey(
                Base64.getEncoder().encodeToString(
                        anotherKeyPair.getPublic().getEncoded()
                )
        );

        boolean result =
                signatureService.verifyTransactionSignature(
                        transaction
                );

        assertFalse(result);
    }

    private String createValidSignature(
            Transaction transaction
    ) throws Exception {

        String payload =
                transaction.getId()
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

        Signature signer =
                Signature.getInstance(
                        "SHA256withECDSA"
                );

        PrivateKey privateKey =
                keyPair.getPrivate();

        signer.initSign(privateKey);

        signer.update(
                payload.getBytes(StandardCharsets.UTF_8)
        );

        byte[] signatureBytes =
                signer.sign();

        return Base64.getEncoder().encodeToString(
                signatureBytes
        );
    }
}