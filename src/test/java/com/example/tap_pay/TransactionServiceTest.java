package com.example.tap_pay;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DeviceRepository deviceRepository;

    private TransactionServiceImpl transactionService;

    private UUID txId;
    private UUID senderId;
    private UUID receiverId;
    private UUID deviceId;

    private User sender;
    private User receiver;
    private Device senderDevice;

    private final long validTimestamp =
            System.currentTimeMillis();

    @BeforeEach
    void setUp() {

        transactionService =
                new TransactionServiceImpl(
                        transactionRepository,
                        userRepository,
                        deviceRepository
                );

        txId = UUID.randomUUID();
        senderId = UUID.randomUUID();
        receiverId = UUID.randomUUID();
        deviceId = UUID.randomUUID();

        sender = new User();
        sender.setId(senderId);
        sender.setActive(true);

        receiver = new User();
        receiver.setId(receiverId);
        receiver.setActive(true);

        senderDevice = new Device();
        senderDevice.setId(deviceId);
        senderDevice.setUser(sender);
        senderDevice.setDeviceIdentifier("DEVICE-001");
        senderDevice.setPublicKey("PUBLIC-KEY");
        senderDevice.setKeyAlgorithm("EC");
        senderDevice.setActive(true);
        senderDevice.setRegisteredAt(
                LocalDateTime.now().minusMinutes(10)
        );
        senderDevice.setLastSeenAt(
                LocalDateTime.now()
        );
    }


    // =========================================================
    // CREATE TRANSACTION - SUCCESS
    // =========================================================

    @Test
    void createTransaction_shouldCreateSuccessfully() {

        CreateTransactionRequest request =
                new CreateTransactionRequest(
                        txId,
                        senderId,
                        receiverId,
                        deviceId,
                        new BigDecimal("100.00"),
                        "INR",
                        "nonce-001",
                        validTimestamp,
                        "signature-001"
                );

        when(transactionRepository.existsById(txId))
                .thenReturn(false);

        when(userRepository.findById(senderId))
                .thenReturn(Optional.of(sender));

        when(userRepository.findById(receiverId))
                .thenReturn(Optional.of(receiver));

        when(deviceRepository
                .findByIdAndActiveTrue(deviceId))
                .thenReturn(Optional.of(senderDevice));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        TransactionResponse response =
                transactionService.createTransaction(request);

        assertNotNull(response);

        assertEquals(txId, response.txId());
        assertEquals(senderId, response.senderId());
        assertEquals(receiverId, response.receiverId());

        assertEquals(
                new BigDecimal("100.00"),
                response.amount()
        );

        assertEquals("INR", response.currency());

        assertEquals(
                TransactionStatus.RECEIVED,
                response.status()
        );

        assertNotNull(response.receivedAt());
        assertNull(response.settledAt());
        assertNull(response.rejectionReason());

        verify(transactionRepository)
                .existsById(txId);

        verify(userRepository)
                .findById(senderId);

        verify(userRepository)
                .findById(receiverId);

        verify(deviceRepository)
                .findByIdAndActiveTrue(deviceId);

        verify(transactionRepository)
                .save(any(Transaction.class));
    }


    @Test
    void createTransaction_shouldStoreCurrencyInUppercase() {

        CreateTransactionRequest request =
                new CreateTransactionRequest(
                        txId,
                        senderId,
                        receiverId,
                        deviceId,
                        new BigDecimal("250.00"),
                        "inr",
                        "nonce-002",
                        validTimestamp,
                        "signature-002"
                );

        when(transactionRepository.existsById(txId))
                .thenReturn(false);

        when(userRepository.findById(senderId))
                .thenReturn(Optional.of(sender));

        when(userRepository.findById(receiverId))
                .thenReturn(Optional.of(receiver));

        when(deviceRepository
                .findByIdAndActiveTrue(deviceId))
                .thenReturn(Optional.of(senderDevice));

        ArgumentCaptor<Transaction> captor =
                ArgumentCaptor.forClass(Transaction.class);

        when(transactionRepository.save(captor.capture()))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        transactionService.createTransaction(request);

        Transaction savedTransaction =
                captor.getValue();

        assertEquals(
                "INR",
                savedTransaction.getCurrency()
        );
    }


    // =========================================================
    // TRANSACTION ID VALIDATION
    // =========================================================

    @Test
    void createTransaction_shouldRejectNullTransactionId() {

        CreateTransactionRequest request =
                new CreateTransactionRequest(
                        null,
                        senderId,
                        receiverId,
                        deviceId,
                        new BigDecimal("100.00"),
                        "INR",
                        "nonce-003",
                        validTimestamp,
                        "signature"
                );

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> transactionService
                                .createTransaction(request)
                );

        assertEquals(
                "Transaction ID is required",
                exception.getMessage()
        );

        verifyNoInteractions(
                transactionRepository,
                userRepository,
                deviceRepository
        );
    }


    @Test
    void createTransaction_shouldRejectDuplicateTransaction() {

        CreateTransactionRequest request =
                new CreateTransactionRequest(
                        txId,
                        senderId,
                        receiverId,
                        deviceId,
                        new BigDecimal("100.00"),
                        "INR",
                        "nonce-004",
                        validTimestamp,
                        "signature"
                );

        when(transactionRepository.existsById(txId))
                .thenReturn(true);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> transactionService
                                .createTransaction(request)
                );

        assertEquals(
                "Transaction already exists",
                exception.getMessage()
        );

        verify(transactionRepository)
                .existsById(txId);

        verify(userRepository, never())
                .findById(any());

        verify(transactionRepository, never())
                .save(any());
    }


    // =========================================================
    // AMOUNT VALIDATION
    // =========================================================

    @Test
    void createTransaction_shouldRejectNullAmount() {

        CreateTransactionRequest request =
                new CreateTransactionRequest(
                        txId,
                        senderId,
                        receiverId,
                        deviceId,
                        null,
                        "INR",
                        "nonce-005",
                        validTimestamp,
                        "signature"
                );

        when(transactionRepository.existsById(txId))
                .thenReturn(false);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> transactionService
                                .createTransaction(request)
                );

        assertEquals(
                "Transaction amount must be greater than zero",
                exception.getMessage()
        );

        verify(userRepository, never())
                .findById(any());

        verify(transactionRepository, never())
                .save(any());
    }


    @Test
    void createTransaction_shouldRejectZeroAmount() {

        CreateTransactionRequest request =
                new CreateTransactionRequest(
                        txId,
                        senderId,
                        receiverId,
                        deviceId,
                        BigDecimal.ZERO,
                        "INR",
                        "nonce-006",
                        validTimestamp,
                        "signature"
                );

        when(transactionRepository.existsById(txId))
                .thenReturn(false);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> transactionService
                                .createTransaction(request)
                );

        assertEquals(
                "Transaction amount must be greater than zero",
                exception.getMessage()
        );
    }


    @Test
    void createTransaction_shouldRejectNegativeAmount() {

        CreateTransactionRequest request =
                new CreateTransactionRequest(
                        txId,
                        senderId,
                        receiverId,
                        deviceId,
                        new BigDecimal("-10.00"),
                        "INR",
                        "nonce-007",
                        validTimestamp,
                        "signature"
                );

        when(transactionRepository.existsById(txId))
                .thenReturn(false);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> transactionService
                                .createTransaction(request)
                );

        assertEquals(
                "Transaction amount must be greater than zero",
                exception.getMessage()
        );
    }


    // =========================================================
    // CURRENCY VALIDATION
    // =========================================================

    @Test
    void createTransaction_shouldRejectNullCurrency() {

        CreateTransactionRequest request =
                new CreateTransactionRequest(
                        txId,
                        senderId,
                        receiverId,
                        deviceId,
                        new BigDecimal("100.00"),
                        null,
                        "nonce-008",
                        validTimestamp,
                        "signature"
                );

        when(transactionRepository.existsById(txId))
                .thenReturn(false);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> transactionService
                                .createTransaction(request)
                );

        assertEquals(
                "Currency is required",
                exception.getMessage()
        );
    }


    @Test
    void createTransaction_shouldRejectBlankCurrency() {

        CreateTransactionRequest request =
                new CreateTransactionRequest(
                        txId,
                        senderId,
                        receiverId,
                        deviceId,
                        new BigDecimal("100.00"),
                        "   ",
                        "nonce-009",
                        validTimestamp,
                        "signature"
                );

        when(transactionRepository.existsById(txId))
                .thenReturn(false);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> transactionService
                                .createTransaction(request)
                );

        assertEquals(
                "Currency is required",
                exception.getMessage()
        );
    }


    @Test
    void createTransaction_shouldRejectCurrencyWithWrongLength() {

        CreateTransactionRequest request =
                new CreateTransactionRequest(
                        txId,
                        senderId,
                        receiverId,
                        deviceId,
                        new BigDecimal("100.00"),
                        "US",
                        "nonce-010",
                        validTimestamp,
                        "signature"
                );

        when(transactionRepository.existsById(txId))
                .thenReturn(false);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> transactionService
                                .createTransaction(request)
                );

        assertEquals(
                "Currency must contain exactly 3 characters",
                exception.getMessage()
        );
    }


    // =========================================================
    // SENDER VALIDATION
    // =========================================================

    @Test
    void createTransaction_shouldRejectWhenSenderNotFound() {

        CreateTransactionRequest request =
                new CreateTransactionRequest(
                        txId,
                        senderId,
                        receiverId,
                        deviceId,
                        new BigDecimal("100.00"),
                        "INR",
                        "nonce-011",
                        validTimestamp,
                        "signature"
                );

        when(transactionRepository.existsById(txId))
                .thenReturn(false);

        when(userRepository.findById(senderId))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> transactionService
                                .createTransaction(request)
                );

        assertEquals(
                "Sender not found",
                exception.getMessage()
        );

        verify(userRepository)
                .findById(senderId);

        verify(transactionRepository, never())
                .save(any());
    }


    @Test
    void createTransaction_shouldRejectInactiveSender() {

        sender.setActive(false);

        CreateTransactionRequest request =
                new CreateTransactionRequest(
                        txId,
                        senderId,
                        receiverId,
                        deviceId,
                        new BigDecimal("100.00"),
                        "INR",
                        "nonce-012",
                        validTimestamp,
                        "signature"
                );

        when(transactionRepository.existsById(txId))
                .thenReturn(false);

        when(userRepository.findById(senderId))
                .thenReturn(Optional.of(sender));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> transactionService
                                .createTransaction(request)
                );

        assertEquals(
                "Sender account is inactive",
                exception.getMessage()
        );

        verify(userRepository)
                .findById(senderId);

        verify(userRepository, never())
                .findById(receiverId);
    }


    // =========================================================
    // RECEIVER VALIDATION
    // =========================================================

    @Test
    void createTransaction_shouldRejectWhenReceiverNotFound() {

        CreateTransactionRequest request =
                new CreateTransactionRequest(
                        txId,
                        senderId,
                        receiverId,
                        deviceId,
                        new BigDecimal("100.00"),
                        "INR",
                        "nonce-013",
                        validTimestamp,
                        "signature"
                );

        when(transactionRepository.existsById(txId))
                .thenReturn(false);

        when(userRepository.findById(senderId))
                .thenReturn(Optional.of(sender));

        when(userRepository.findById(receiverId))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> transactionService
                                .createTransaction(request)
                );

        assertEquals(
                "Receiver not found",
                exception.getMessage()
        );

        verify(transactionRepository, never())
                .save(any());
    }


    @Test
    void createTransaction_shouldRejectInactiveReceiver() {

        receiver.setActive(false);

        CreateTransactionRequest request =
                new CreateTransactionRequest(
                        txId,
                        senderId,
                        receiverId,
                        deviceId,
                        new BigDecimal("100.00"),
                        "INR",
                        "nonce-014",
                        validTimestamp,
                        "signature"
                );

        when(transactionRepository.existsById(txId))
                .thenReturn(false);

        when(userRepository.findById(senderId))
                .thenReturn(Optional.of(sender));

        when(userRepository.findById(receiverId))
                .thenReturn(Optional.of(receiver));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> transactionService
                                .createTransaction(request)
                );

        assertEquals(
                "Receiver account is inactive",
                exception.getMessage()
        );

        verify(deviceRepository, never())
                .findByIdAndActiveTrue(any());
    }


    @Test
    void createTransaction_shouldRejectSameSenderAndReceiver() {

        User sameUser = new User();
        sameUser.setId(senderId);
        sameUser.setActive(true);

        CreateTransactionRequest request =
                new CreateTransactionRequest(
                        txId,
                        senderId,
                        senderId,
                        deviceId,
                        new BigDecimal("100.00"),
                        "INR",
                        "nonce-015",
                        validTimestamp,
                        "signature"
                );

        when(transactionRepository.existsById(txId))
                .thenReturn(false);

        when(userRepository.findById(senderId))
                .thenReturn(Optional.of(sameUser));

        when(userRepository.findById(senderId))
                .thenReturn(Optional.of(sameUser));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> transactionService
                                .createTransaction(request)
                );

        assertEquals(
                "Sender and receiver cannot be the same",
                exception.getMessage()
        );

        verify(deviceRepository, never())
                .findByIdAndActiveTrue(any());
    }


    // =========================================================
    // DEVICE VALIDATION
    // =========================================================

    @Test
    void createTransaction_shouldRejectInactiveOrMissingDevice() {

        CreateTransactionRequest request =
                new CreateTransactionRequest(
                        txId,
                        senderId,
                        receiverId,
                        deviceId,
                        new BigDecimal("100.00"),
                        "INR",
                        "nonce-016",
                        validTimestamp,
                        "signature"
                );

        when(transactionRepository.existsById(txId))
                .thenReturn(false);

        when(userRepository.findById(senderId))
                .thenReturn(Optional.of(sender));

        when(userRepository.findById(receiverId))
                .thenReturn(Optional.of(receiver));

        when(deviceRepository
                .findByIdAndActiveTrue(deviceId))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> transactionService
                                .createTransaction(request)
                );

        assertEquals(
                "Active sender device not found",
                exception.getMessage()
        );

        verify(transactionRepository, never())
                .save(any());
    }


    @Test
    void createTransaction_shouldRejectDeviceBelongingToAnotherUser() {

        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());
        anotherUser.setActive(true);

        senderDevice.setUser(anotherUser);

        CreateTransactionRequest request =
                new CreateTransactionRequest(
                        txId,
                        senderId,
                        receiverId,
                        deviceId,
                        new BigDecimal("100.00"),
                        "INR",
                        "nonce-017",
                        validTimestamp,
                        "signature"
                );

        when(transactionRepository.existsById(txId))
                .thenReturn(false);

        when(userRepository.findById(senderId))
                .thenReturn(Optional.of(sender));

        when(userRepository.findById(receiverId))
                .thenReturn(Optional.of(receiver));

        when(deviceRepository
                .findByIdAndActiveTrue(deviceId))
                .thenReturn(Optional.of(senderDevice));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> transactionService
                                .createTransaction(request)
                );

        assertEquals(
                "Device does not belong to sender",
                exception.getMessage()
        );

        verify(transactionRepository, never())
                .save(any());
    }


    // =========================================================
    // TIMESTAMP VALIDATION
    // =========================================================

    @Test
    void createTransaction_shouldRejectNullTimestamp() {

        CreateTransactionRequest request =
                new CreateTransactionRequest(
                        txId,
                        senderId,
                        receiverId,
                        deviceId,
                        new BigDecimal("100.00"),
                        "INR",
                        "nonce-018",
                        null,
                        "signature"
                );

        when(transactionRepository.existsById(txId))
                .thenReturn(false);

        when(userRepository.findById(senderId))
                .thenReturn(Optional.of(sender));

        when(userRepository.findById(receiverId))
                .thenReturn(Optional.of(receiver));

        when(deviceRepository
                .findByIdAndActiveTrue(deviceId))
                .thenReturn(Optional.of(senderDevice));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> transactionService
                                .createTransaction(request)
                );

        assertEquals(
                "Transaction timestamp is required",
                exception.getMessage()
        );

        verify(transactionRepository, never())
                .save(any());
    }


    @Test
    void createTransaction_shouldRejectTimestampTooFarInFuture() {

        long futureTimestamp =
                System.currentTimeMillis()
                        + (10 * 60 * 1000L);

        CreateTransactionRequest request =
                new CreateTransactionRequest(
                        txId,
                        senderId,
                        receiverId,
                        deviceId,
                        new BigDecimal("100.00"),
                        "INR",
                        "nonce-019",
                        futureTimestamp,
                        "signature"
                );

        when(transactionRepository.existsById(txId))
                .thenReturn(false);

        when(userRepository.findById(senderId))
                .thenReturn(Optional.of(sender));

        when(userRepository.findById(receiverId))
                .thenReturn(Optional.of(receiver));

        when(deviceRepository
                .findByIdAndActiveTrue(deviceId))
                .thenReturn(Optional.of(senderDevice));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> transactionService
                                .createTransaction(request)
                );

        assertEquals(
                "Transaction timestamp is invalid",
                exception.getMessage()
        );
    }


    @Test
    void createTransaction_shouldAcceptTimestampWithinFiveMinutesFuture() {

        long futureTimestamp =
                System.currentTimeMillis()
                        + (4 * 60 * 1000L);

        CreateTransactionRequest request =
                new CreateTransactionRequest(
                        txId,
                        senderId,
                        receiverId,
                        deviceId,
                        new BigDecimal("100.00"),
                        "INR",
                        "nonce-020",
                        futureTimestamp,
                        "signature"
                );

        when(transactionRepository.existsById(txId))
                .thenReturn(false);

        when(userRepository.findById(senderId))
                .thenReturn(Optional.of(sender));

        when(userRepository.findById(receiverId))
                .thenReturn(Optional.of(receiver));

        when(deviceRepository
                .findByIdAndActiveTrue(deviceId))
                .thenReturn(Optional.of(senderDevice));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        TransactionResponse response =
                transactionService.createTransaction(request);

        assertNotNull(response);
        assertEquals(
                TransactionStatus.RECEIVED,
                response.status()
        );
    }


    // =========================================================
    // GET TRANSACTION
    // =========================================================

    @Test
    void getTransaction_shouldReturnSuccessfully() {

        Transaction transaction =
                createTransactionEntity();

        when(transactionRepository.findById(txId))
                .thenReturn(Optional.of(transaction));

        TransactionResponse response =
                transactionService.getTransaction(txId);

        assertNotNull(response);

        assertEquals(txId, response.txId());
        assertEquals(senderId, response.senderId());
        assertEquals(receiverId, response.receiverId());

        assertEquals(
                new BigDecimal("100.00"),
                response.amount()
        );

        assertEquals("INR", response.currency());

        assertEquals(
                TransactionStatus.RECEIVED,
                response.status()
        );

        verify(transactionRepository)
                .findById(txId);
    }


    @Test
    void getTransaction_shouldRejectWhenNotFound() {

        when(transactionRepository.findById(txId))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> transactionService
                                .getTransaction(txId)
                );

        assertEquals(
                "Transaction not found",
                exception.getMessage()
        );
    }


    // =========================================================
    // GET SENDER TRANSACTIONS
    // =========================================================

    @Test
    void getSenderTransactions_shouldReturnTransactions() {

        Transaction transaction =
                createTransactionEntity();

        when(userRepository.existsById(senderId))
                .thenReturn(true);

        when(transactionRepository
                .findBySenderId(senderId))
                .thenReturn(List.of(transaction));

        List<TransactionResponse> responses =
                transactionService
                        .getSenderTransactions(senderId);

        assertNotNull(responses);
        assertEquals(1, responses.size());

        TransactionResponse response =
                responses.get(0);

        assertEquals(txId, response.txId());
        assertEquals(senderId, response.senderId());
        assertEquals(receiverId, response.receiverId());

        verify(userRepository)
                .existsById(senderId);

        verify(transactionRepository)
                .findBySenderId(senderId);
    }


    @Test
    void getSenderTransactions_shouldRejectUnknownSender() {

        when(userRepository.existsById(senderId))
                .thenReturn(false);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> transactionService
                                .getSenderTransactions(senderId)
                );

        assertEquals(
                "Sender not found",
                exception.getMessage()
        );

        verify(transactionRepository, never())
                .findBySenderId(any());
    }


    // =========================================================
    // GET RECEIVER TRANSACTIONS
    // =========================================================

    @Test
    void getReceiverTransactions_shouldReturnTransactions() {

        Transaction transaction =
                createTransactionEntity();

        when(userRepository.existsById(receiverId))
                .thenReturn(true);

        when(transactionRepository
                .findByReceiverId(receiverId))
                .thenReturn(List.of(transaction));

        List<TransactionResponse> responses =
                transactionService
                        .getReceiverTransactions(receiverId);

        assertNotNull(responses);
        assertEquals(1, responses.size());

        TransactionResponse response =
                responses.get(0);

        assertEquals(txId, response.txId());
        assertEquals(senderId, response.senderId());
        assertEquals(receiverId, response.receiverId());

        verify(userRepository)
                .existsById(receiverId);

        verify(transactionRepository)
                .findByReceiverId(receiverId);
    }


    @Test
    void getReceiverTransactions_shouldRejectUnknownReceiver() {

        when(userRepository.existsById(receiverId))
                .thenReturn(false);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> transactionService
                                .getReceiverTransactions(receiverId)
                );

        assertEquals(
                "Receiver not found",
                exception.getMessage()
        );

        verify(transactionRepository, never())
                .findByReceiverId(any());
    }


    // =========================================================
    // GET TRANSACTIONS BY STATUS
    // =========================================================

    @Test
    void getTransactionsByStatus_shouldReturnTransactions() {

        Transaction transaction =
                createTransactionEntity();

        when(transactionRepository
                .findByStatus(TransactionStatus.RECEIVED))
                .thenReturn(List.of(transaction));

        List<TransactionResponse> responses =
                transactionService.getTransactionsByStatus(
                        TransactionStatus.RECEIVED
                );

        assertNotNull(responses);
        assertEquals(1, responses.size());

        assertEquals(
                txId,
                responses.get(0).txId()
        );

        assertEquals(
                TransactionStatus.RECEIVED,
                responses.get(0).status()
        );

        verify(transactionRepository)
                .findByStatus(TransactionStatus.RECEIVED);
    }


    @Test
    void getTransactionsByStatus_shouldReturnEmptyList() {

        when(transactionRepository
                .findByStatus(TransactionStatus.SETTLED))
                .thenReturn(List.of());

        List<TransactionResponse> responses =
                transactionService.getTransactionsByStatus(
                        TransactionStatus.SETTLED
                );

        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(transactionRepository)
                .findByStatus(TransactionStatus.SETTLED);
    }


    @Test
    void getTransactionsByStatus_shouldRejectNullStatus() {

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> transactionService
                                .getTransactionsByStatus(null)
                );

        assertEquals(
                "Transaction status is required",
                exception.getMessage()
        );

        verifyNoInteractions(transactionRepository);
    }


    // =========================================================
    // HELPER
    // =========================================================

    private Transaction createTransactionEntity() {

        Transaction transaction =
                new Transaction();

        transaction.setId(txId);
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setSenderDevice(senderDevice);
        transaction.setAmount(
                new BigDecimal("100.00")
        );
        transaction.setCurrency("INR");
        transaction.setNonce("nonce-test");
        transaction.setSignature("signature-test");
        transaction.setClientTimestamp(
                validTimestamp
        );
        transaction.setReceivedAt(
                LocalDateTime.now()
        );
        transaction.setStatus(
                TransactionStatus.RECEIVED
        );

        return transaction;
    }
}