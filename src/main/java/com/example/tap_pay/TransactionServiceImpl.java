package com.example.tap_pay;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;

    public TransactionServiceImpl(
            TransactionRepository transactionRepository,
            UserRepository userRepository,
            DeviceRepository deviceRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
    }

    @Override
    @Transactional
    public TransactionResponse createTransaction(
            CreateTransactionRequest request
    ) {

        // 1. Validate transaction ID
        if (request.txId() == null) {
            throw new RuntimeException(
                    "Transaction ID is required"
            );
        }

        // Prevent duplicate transaction
        if (transactionRepository.existsById(request.txId())) {
            throw new RuntimeException(
                    "Transaction already exists"
            );
        }

        // 2. Validate amount
        validateAmount(request.amount());

        // 3. Validate currency
        validateCurrency(request.currency());

        // 4. Find sender
        User sender = userRepository.findById(
                request.senderId()
        ).orElseThrow(() ->
                new RuntimeException(
                        "Sender not found"
                )
        );

        // 5. Sender must be active
        if (!sender.isActive()) {
            throw new RuntimeException(
                    "Sender account is inactive"
            );
        }

        // 6. Find receiver
        User receiver = userRepository.findById(
                request.receiverId()
        ).orElseThrow(() ->
                new RuntimeException(
                        "Receiver not found"
                )
        );

        // 7. Receiver must be active
        if (!receiver.isActive()) {
            throw new RuntimeException(
                    "Receiver account is inactive"
            );
        }

        // Sender and receiver cannot be same
        if (sender.getId().equals(receiver.getId())) {
            throw new RuntimeException(
                    "Sender and receiver cannot be the same"
            );
        }

        // 8. Find sender device
        Device senderDevice = deviceRepository
                .findByIdAndActiveTrue(
                        request.senderDeviceId()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Active sender device not found"
                        )
                );

        // 9. Make sure device actually belongs to sender
        if (!senderDevice.getUser()
                .getId()
                .equals(sender.getId())) {

            throw new RuntimeException(
                    "Device does not belong to sender"
            );
        }

        // 10. Validate timestamp
        validateTimestamp(request.timestamp());

        // 11. Create transaction
        Transaction transaction = new Transaction();

        transaction.setId(request.txId());
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setSenderDevice(senderDevice);
        transaction.setAmount(request.amount());
        transaction.setCurrency(
                request.currency().toUpperCase()
        );
        transaction.setNonce(request.nonce());
        transaction.setSignature(request.signature());
        transaction.setClientTimestamp(
                request.timestamp()
        );

        transaction.setReceivedAt(
                LocalDateTime.now()
        );

        transaction.setStatus(
                TransactionStatus.RECEIVED
        );

        // 12. Save transaction
        Transaction savedTransaction =
                transactionRepository.save(transaction);

        return mapToResponse(savedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(
            UUID transactionId
    ) {

        Transaction transaction =
                transactionRepository.findById(transactionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Transaction not found"
                                )
                        );

        return mapToResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getSenderTransactions(
            UUID senderId
    ) {

        // Make sure sender exists
        if (!userRepository.existsById(senderId)) {
            throw new RuntimeException(
                    "Sender not found"
            );
        }

        return transactionRepository
                .findBySenderId(senderId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getReceiverTransactions(
            UUID receiverId
    ) {

        // Make sure receiver exists
        if (!userRepository.existsById(receiverId)) {
            throw new RuntimeException(
                    "Receiver not found"
            );
        }

        return transactionRepository
                .findByReceiverId(receiverId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByStatus(
            TransactionStatus status
    ) {

        if (status == null) {
            throw new RuntimeException(
                    "Transaction status is required"
            );
        }

        return transactionRepository
                .findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void validateAmount(BigDecimal amount) {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new RuntimeException(
                    "Transaction amount must be greater than zero"
            );
        }
    }

    private void validateCurrency(String currency) {

        if (currency == null ||
                currency.isBlank()) {

            throw new RuntimeException(
                    "Currency is required"
            );
        }

        if (currency.length() != 3) {
            throw new RuntimeException(
                    "Currency must contain exactly 3 characters"
            );
        }
    }

    private void validateTimestamp(Long timestamp) {

        if (timestamp == null) {
            throw new RuntimeException(
                    "Transaction timestamp is required"
            );
        }

        long currentTime = System.currentTimeMillis();

        /*
         * Allow a small amount of clock difference.
         * Detailed transaction-age validation will be handled
         * by the security/validation layer later.
         */
        long allowedFutureDifference =
                5 * 60 * 1000L;

        if (timestamp > currentTime + allowedFutureDifference) {
            throw new RuntimeException(
                    "Transaction timestamp is invalid"
            );
        }
    }

    private TransactionResponse mapToResponse(
            Transaction transaction
    ) {

        return new TransactionResponse(
                transaction.getId(),
                transaction.getSender().getId(),
                transaction.getReceiver().getId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getStatus(),
                transaction.getReceivedAt(),
                transaction.getSettledAt(),
                transaction.getRejectionReason()
        );
    }
}