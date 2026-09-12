package com.example.tap_pay;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SettlementServiceImpl implements SettlementService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final SettlementRepository settlementRepository;

    public SettlementServiceImpl(
            TransactionRepository transactionRepository,
            WalletRepository walletRepository,
            SettlementRepository settlementRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.settlementRepository = settlementRepository;
    }

    @Override
    @Transactional
    public Settlement settleTransaction(UUID transactionId) {

        // 1. Find transaction
        Transaction transaction =
                transactionRepository.findById(transactionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Transaction not found"
                                )
                        );

        // 2. If already settled, return existing settlement
        if (transaction.getStatus() ==
                TransactionStatus.SETTLED) {

            return settlementRepository
                    .findByTransactionId(transactionId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Settlement record not found"
                            )
                    );
        }

        // 3. Only RECEIVED transactions can be settled
        if (transaction.getStatus() !=
                TransactionStatus.RECEIVED) {

            throw new RuntimeException(
                    "Transaction cannot be settled in its current state"
            );
        }

        // 4. Get sender wallet
        Wallet senderWallet =
                walletRepository
                        .findWithLockByUserId(
                                transaction.getSender().getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Sender wallet not found"
                                )
                        );

        // 5. Get receiver wallet
        Wallet receiverWallet =
                walletRepository
                        .findWithLockByUserId(
                                transaction.getReceiver().getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Receiver wallet not found"
                                )
                        );

        BigDecimal amount =
                transaction.getAmount();

        // 6. Validate amount
        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            transaction.setStatus(
                    TransactionStatus.REJECTED
            );

            transaction.setRejectionReason(
                    RejectionReason.INVALID_AMOUNT
            );

            transactionRepository.save(transaction);

            throw new RuntimeException(
                    "Invalid transaction amount"
            );
        }

        // 7. Check sender balance
        if (senderWallet
                .getAvailableBalance()
                .compareTo(amount) < 0) {

            transaction.setStatus(
                    TransactionStatus.REJECTED
            );

            transaction.setRejectionReason(
                    RejectionReason.INSUFFICIENT_BALANCE
            );

            transactionRepository.save(transaction);

            throw new RuntimeException(
                    "Insufficient sender balance"
            );
        }

        // 8. Deduct money from sender
        senderWallet.setAvailableBalance(
                senderWallet
                        .getAvailableBalance()
                        .subtract(amount)
        );

        // 9. Credit receiver
        receiverWallet.setAvailableBalance(
                receiverWallet
                        .getAvailableBalance()
                        .add(amount)
        );

        // 10. Update wallet timestamps
        LocalDateTime now =
                LocalDateTime.now();

        senderWallet.setUpdatedAt(now);
        receiverWallet.setUpdatedAt(now);

        // 11. Save both wallets
        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        // 12. Mark transaction as settled
        transaction.setStatus(
                TransactionStatus.SETTLED
        );

        transaction.setSettledAt(now);

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        // 13. Create settlement record
        Settlement settlement =
                new Settlement();

        settlement.setTransaction(
                savedTransaction
        );

        settlement.setSenderWalletId(
                senderWallet.getId()
        );

        settlement.setReceiverWalletId(
                receiverWallet.getId()
        );

        settlement.setAmount(amount);

        settlement.setSettledAt(now);

        settlement.setStatus(
                SettlementStatus.SUCCESS
        );

        // 14. Generate settlement reference
        settlement.setSettlementReference(
                generateSettlementReference(
                        transaction.getId()
                )
        );

        // 15. Save settlement
        return settlementRepository.save(
                settlement
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Settlement getSettlement(
            UUID transactionId
    ) {

        return settlementRepository
                .findByTransactionId(transactionId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Settlement not found"
                        )
                );
    }

    private String generateSettlementReference(
            UUID transactionId
    ) {

        return "SET-" +
                transactionId.toString()
                        .replace("-", "")
                        .substring(0, 16)
                        .toUpperCase();
    }
}