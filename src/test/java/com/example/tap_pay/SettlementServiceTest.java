package com.example.tap_pay;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private SettlementRepository settlementRepository;

    private SettlementServiceImpl settlementService;

    private UUID transactionId;
    private UUID senderId;
    private UUID receiverId;
    private UUID senderWalletId;
    private UUID receiverWalletId;

    private User sender;
    private User receiver;

    private Wallet senderWallet;
    private Wallet receiverWallet;

    private Transaction transaction;

    @BeforeEach
    void setUp() {

        settlementService = new SettlementServiceImpl(
                transactionRepository,
                walletRepository,
                settlementRepository
        );

        transactionId = UUID.randomUUID();
        senderId = UUID.randomUUID();
        receiverId = UUID.randomUUID();

        senderWalletId = UUID.randomUUID();
        receiverWalletId = UUID.randomUUID();

        sender = new User();
        sender.setId(senderId);
        sender.setActive(true);

        receiver = new User();
        receiver.setId(receiverId);
        receiver.setActive(true);

        senderWallet = new Wallet();
        senderWallet.setId(senderWalletId);
        senderWallet.setUser(sender);
        senderWallet.setAvailableBalance(
                new BigDecimal("1000.00")
        );
        senderWallet.setOfflineLimit(
                new BigDecimal("500.00")
        );
        senderWallet.setOfflineUsed(
                BigDecimal.ZERO
        );

        receiverWallet = new Wallet();
        receiverWallet.setId(receiverWalletId);
        receiverWallet.setUser(receiver);
        receiverWallet.setAvailableBalance(
                new BigDecimal("200.00")
        );
        receiverWallet.setOfflineLimit(
                new BigDecimal("500.00")
        );
        receiverWallet.setOfflineUsed(
                BigDecimal.ZERO
        );

        transaction = new Transaction();

        transaction.setId(transactionId);
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setAmount(
                new BigDecimal("100.00")
        );
        transaction.setCurrency("INR");
        transaction.setNonce("nonce-123");
        transaction.setSignature("signature");
        transaction.setClientTimestamp(
                System.currentTimeMillis()
        );
        transaction.setStatus(
                TransactionStatus.RECEIVED
        );
    }

    // ---------------------------------------------------------
    // Successful settlement
    // ---------------------------------------------------------

    @Test
    void settleTransaction_shouldSuccessfullyMoveMoneyAndCreateSettlement() {

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        when(walletRepository.findWithLockByUserId(senderId))
                .thenReturn(Optional.of(senderWallet));

        when(walletRepository.findWithLockByUserId(receiverId))
                .thenReturn(Optional.of(receiverWallet));

        when(transactionRepository.save(transaction))
                .thenReturn(transaction);

        when(settlementRepository.save(any(Settlement.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        Settlement result =
                settlementService.settleTransaction(transactionId);

        assertNotNull(result);

        assertEquals(
                new BigDecimal("900.00"),
                senderWallet.getAvailableBalance()
        );

        assertEquals(
                new BigDecimal("300.00"),
                receiverWallet.getAvailableBalance()
        );

        assertEquals(
                TransactionStatus.SETTLED,
                transaction.getStatus()
        );

        assertNotNull(transaction.getSettledAt());

        assertEquals(
                SettlementStatus.SUCCESS,
                result.getStatus()
        );

        assertEquals(
                new BigDecimal("100.00"),
                result.getAmount()
        );

        assertEquals(
                senderWalletId,
                result.getSenderWalletId()
        );

        assertEquals(
                receiverWalletId,
                result.getReceiverWalletId()
        );

        assertNotNull(result.getSettledAt());

        assertNotNull(result.getSettlementReference());

        assertTrue(
                result.getSettlementReference()
                        .startsWith("SET-")
        );

        verify(walletRepository)
                .save(senderWallet);

        verify(walletRepository)
                .save(receiverWallet);

        verify(transactionRepository)
                .save(transaction);

        verify(settlementRepository)
                .save(any(Settlement.class));
    }

    @Test
    void settleTransaction_shouldGenerateCorrectSettlementReference() {

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        when(walletRepository.findWithLockByUserId(senderId))
                .thenReturn(Optional.of(senderWallet));

        when(walletRepository.findWithLockByUserId(receiverId))
                .thenReturn(Optional.of(receiverWallet));

        when(transactionRepository.save(transaction))
                .thenReturn(transaction);

        when(settlementRepository.save(any(Settlement.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        Settlement result =
                settlementService.settleTransaction(transactionId);

        String expectedReference =
                "SET-" +
                        transactionId
                                .toString()
                                .replace("-", "")
                                .substring(0, 16)
                                .toUpperCase();

        assertEquals(
                expectedReference,
                result.getSettlementReference()
        );
    }

    // ---------------------------------------------------------
    // Already settled transaction
    // ---------------------------------------------------------

    @Test
    void settleTransaction_shouldReturnExistingSettlementWhenAlreadySettled() {

        transaction.setStatus(
                TransactionStatus.SETTLED
        );

        Settlement existingSettlement =
                new Settlement();

        existingSettlement.setTransaction(transaction);
        existingSettlement.setAmount(
                new BigDecimal("100.00")
        );
        existingSettlement.setStatus(
                SettlementStatus.SUCCESS
        );
        existingSettlement.setSettlementReference(
                "SET-EXISTING123456"
        );

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        when(settlementRepository.findByTransactionId(
                transactionId
        )).thenReturn(Optional.of(existingSettlement));

        Settlement result =
                settlementService.settleTransaction(transactionId);

        assertSame(
                existingSettlement,
                result
        );

        verify(
                walletRepository,
                never()
        ).findWithLockByUserId(any());

        verify(
                walletRepository,
                never()
        ).save(any());

        verify(
                transactionRepository,
                never()
        ).save(any());

        verify(
                settlementRepository,
                never()
        ).save(any());
    }

    @Test
    void settleTransaction_shouldFailWhenExistingSettlementRecordIsMissing() {

        transaction.setStatus(
                TransactionStatus.SETTLED
        );

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        when(settlementRepository.findByTransactionId(
                transactionId
        )).thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> settlementService
                                .settleTransaction(transactionId)
                );

        assertEquals(
                "Settlement record not found",
                exception.getMessage()
        );
    }

    // ---------------------------------------------------------
    // Invalid transaction states
    // ---------------------------------------------------------

    @Test
    void settleTransaction_shouldRejectNonReceivedTransaction() {

        transaction.setStatus(
                TransactionStatus.REJECTED
        );

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> settlementService
                                .settleTransaction(transactionId)
                );

        assertEquals(
                "Transaction cannot be settled in its current state",
                exception.getMessage()
        );

        verify(
                walletRepository,
                never()
        ).findWithLockByUserId(any());
    }

    @Test
    void settleTransaction_shouldRejectValidatingTransaction() {

        transaction.setStatus(
                TransactionStatus.VALIDATING
        );

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> settlementService
                                .settleTransaction(transactionId)
                );

        assertEquals(
                "Transaction cannot be settled in its current state",
                exception.getMessage()
        );
    }

    @Test
    void settleTransaction_shouldRejectReceivedTransactionWithNullStatus() {

        transaction.setStatus(null);

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> settlementService
                                .settleTransaction(transactionId)
                );

        assertEquals(
                "Transaction cannot be settled in its current state",
                exception.getMessage()
        );
    }

    // ---------------------------------------------------------
    // Transaction lookup
    // ---------------------------------------------------------

    @Test
    void settleTransaction_shouldThrowWhenTransactionDoesNotExist() {

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> settlementService
                                .settleTransaction(transactionId)
                );

        assertEquals(
                "Transaction not found",
                exception.getMessage()
        );

        verify(
                walletRepository,
                never()
        ).findWithLockByUserId(any());
    }

    // ---------------------------------------------------------
    // Sender wallet
    // ---------------------------------------------------------

    @Test
    void settleTransaction_shouldThrowWhenSenderWalletDoesNotExist() {

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        when(walletRepository.findWithLockByUserId(senderId))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> settlementService
                                .settleTransaction(transactionId)
                );

        assertEquals(
                "Sender wallet not found",
                exception.getMessage()
        );

        verify(
                walletRepository,
                never()
        ).save(any());
    }

    // ---------------------------------------------------------
    // Receiver wallet
    // ---------------------------------------------------------

    @Test
    void settleTransaction_shouldThrowWhenReceiverWalletDoesNotExist() {

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        when(walletRepository.findWithLockByUserId(senderId))
                .thenReturn(Optional.of(senderWallet));

        when(walletRepository.findWithLockByUserId(receiverId))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> settlementService
                                .settleTransaction(transactionId)
                );

        assertEquals(
                "Receiver wallet not found",
                exception.getMessage()
        );

        verify(
                walletRepository,
                never()
        ).save(any());
    }

    // ---------------------------------------------------------
    // Invalid amount
    // ---------------------------------------------------------

    @Test
    void settleTransaction_shouldRejectNullAmount() {

        transaction.setAmount(null);

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        when(walletRepository.findWithLockByUserId(senderId))
                .thenReturn(Optional.of(senderWallet));

        when(walletRepository.findWithLockByUserId(receiverId))
                .thenReturn(Optional.of(receiverWallet));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> settlementService
                                .settleTransaction(transactionId)
                );

        assertEquals(
                "Invalid transaction amount",
                exception.getMessage()
        );

        assertEquals(
                TransactionStatus.REJECTED,
                transaction.getStatus()
        );

        assertEquals(
                RejectionReason.INVALID_AMOUNT,
                transaction.getRejectionReason()
        );

        verify(transactionRepository)
                .save(transaction);

        verify(
                walletRepository,
                never()
        ).save(any());

        verify(
                settlementRepository,
                never()
        ).save(any());
    }

    @Test
    void settleTransaction_shouldRejectZeroAmount() {

        transaction.setAmount(BigDecimal.ZERO);

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        when(walletRepository.findWithLockByUserId(senderId))
                .thenReturn(Optional.of(senderWallet));

        when(walletRepository.findWithLockByUserId(receiverId))
                .thenReturn(Optional.of(receiverWallet));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> settlementService
                                .settleTransaction(transactionId)
                );

        assertEquals(
                "Invalid transaction amount",
                exception.getMessage()
        );

        assertEquals(
                TransactionStatus.REJECTED,
                transaction.getStatus()
        );

        assertEquals(
                RejectionReason.INVALID_AMOUNT,
                transaction.getRejectionReason()
        );
    }

    @Test
    void settleTransaction_shouldRejectNegativeAmount() {

        transaction.setAmount(
                new BigDecimal("-50.00")
        );

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        when(walletRepository.findWithLockByUserId(senderId))
                .thenReturn(Optional.of(senderWallet));

        when(walletRepository.findWithLockByUserId(receiverId))
                .thenReturn(Optional.of(receiverWallet));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> settlementService
                                .settleTransaction(transactionId)
                );

        assertEquals(
                "Invalid transaction amount",
                exception.getMessage()
        );

        assertEquals(
                TransactionStatus.REJECTED,
                transaction.getStatus()
        );

        assertEquals(
                RejectionReason.INVALID_AMOUNT,
                transaction.getRejectionReason()
        );
    }

    // ---------------------------------------------------------
    // Insufficient balance
    // ---------------------------------------------------------

    @Test
    void settleTransaction_shouldRejectWhenSenderHasInsufficientBalance() {

        senderWallet.setAvailableBalance(
                new BigDecimal("50.00")
        );

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        when(walletRepository.findWithLockByUserId(senderId))
                .thenReturn(Optional.of(senderWallet));

        when(walletRepository.findWithLockByUserId(receiverId))
                .thenReturn(Optional.of(receiverWallet));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> settlementService
                                .settleTransaction(transactionId)
                );

        assertEquals(
                "Insufficient sender balance",
                exception.getMessage()
        );

        assertEquals(
                TransactionStatus.REJECTED,
                transaction.getStatus()
        );

        assertEquals(
                RejectionReason.INSUFFICIENT_BALANCE,
                transaction.getRejectionReason()
        );

        assertEquals(
                new BigDecimal("50.00"),
                senderWallet.getAvailableBalance()
        );

        assertEquals(
                new BigDecimal("200.00"),
                receiverWallet.getAvailableBalance()
        );

        verify(transactionRepository)
                .save(transaction);

        verify(
                walletRepository,
                never()
        ).save(any());

        verify(
                settlementRepository,
                never()
        ).save(any());
    }

    // ---------------------------------------------------------
    // Exact wallet locking
    // ---------------------------------------------------------

    @Test
    void settleTransaction_shouldLoadBothWalletsUsingPessimisticLockRepositoryMethod() {

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        when(walletRepository.findWithLockByUserId(senderId))
                .thenReturn(Optional.of(senderWallet));

        when(walletRepository.findWithLockByUserId(receiverId))
                .thenReturn(Optional.of(receiverWallet));

        when(transactionRepository.save(transaction))
                .thenReturn(transaction);

        when(settlementRepository.save(any(Settlement.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        settlementService.settleTransaction(transactionId);

        verify(walletRepository)
                .findWithLockByUserId(senderId);

        verify(walletRepository)
                .findWithLockByUserId(receiverId);

        verify(
                walletRepository,
                never()
        ).findByUserId(any());
    }

    // ---------------------------------------------------------
    // Settlement record verification
    // ---------------------------------------------------------

    @Test
    void settleTransaction_shouldPopulateSettlementCorrectly() {

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        when(walletRepository.findWithLockByUserId(senderId))
                .thenReturn(Optional.of(senderWallet));

        when(walletRepository.findWithLockByUserId(receiverId))
                .thenReturn(Optional.of(receiverWallet));

        when(transactionRepository.save(transaction))
                .thenReturn(transaction);

        when(settlementRepository.save(any(Settlement.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        Settlement result =
                settlementService.settleTransaction(transactionId);

        assertEquals(
                transaction,
                result.getTransaction()
        );

        assertEquals(
                senderWalletId,
                result.getSenderWalletId()
        );

        assertEquals(
                receiverWalletId,
                result.getReceiverWalletId()
        );

        assertEquals(
                new BigDecimal("100.00"),
                result.getAmount()
        );

        assertEquals(
                SettlementStatus.SUCCESS,
                result.getStatus()
        );

        assertNotNull(result.getSettledAt());

        assertNotNull(result.getSettlementReference());
    }

    // ---------------------------------------------------------
    // getSettlement()
    // ---------------------------------------------------------

    @Test
    void getSettlement_shouldReturnSettlementWhenFound() {

        Settlement settlement =
                new Settlement();

        settlement.setTransaction(transaction);

        settlement.setAmount(
                new BigDecimal("100.00")
        );

        settlement.setStatus(
                SettlementStatus.SUCCESS
        );

        when(settlementRepository.findByTransactionId(
                transactionId
        )).thenReturn(Optional.of(settlement));

        Settlement result =
                settlementService.getSettlement(transactionId);

        assertSame(
                settlement,
                result
        );

        verify(settlementRepository)
                .findByTransactionId(transactionId);
    }

    @Test
    void getSettlement_shouldThrowWhenSettlementDoesNotExist() {

        when(settlementRepository.findByTransactionId(
                transactionId
        )).thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> settlementService
                                .getSettlement(transactionId)
                );

        assertEquals(
                "Settlement not found",
                exception.getMessage()
        );

        verify(settlementRepository)
                .findByTransactionId(transactionId);
    }
}