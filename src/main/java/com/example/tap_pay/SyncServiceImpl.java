package com.example.tap_pay;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;


@Service
public class SyncServiceImpl implements SyncService {

    private final SyncBatchRepository syncBatchRepository;
    private final DeviceRepository deviceRepository;
    private final TransactionRepository transactionRepository;

    private final TransactionService transactionService;
    private final SignatureService signatureService;
    private final NonceService nonceService;
    private final SettlementService settlementService;

    public SyncServiceImpl(
            SyncBatchRepository syncBatchRepository,
            DeviceRepository deviceRepository,
            TransactionRepository transactionRepository,
            TransactionService transactionService,
            SignatureService signatureService,
            NonceService nonceService,
            SettlementService settlementService
    ) {
        this.syncBatchRepository = syncBatchRepository;
        this.deviceRepository = deviceRepository;
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;
        this.signatureService = signatureService;
        this.nonceService = nonceService;
        this.settlementService = settlementService;
    }

    @Override
    @Transactional
    public SyncTransactionsResponse syncTransactions(
            SyncTransactionsRequest request
    ) {

        // 1. Validate request
        if (request == null) {
            throw new RuntimeException(
                    "Sync request is required"
            );
        }

        if (request.deviceId() == null) {
            throw new RuntimeException(
                    "Device ID is required"
            );
        }

        if (request.transactions() == null ||
                request.transactions().isEmpty()) {

            throw new RuntimeException(
                    "No transactions provided for sync"
            );
        }

        // 2. Verify device
        Device device = deviceRepository
                .findByIdAndActiveTrue(
                        request.deviceId()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Active device not found"
                        )
                );

        // 3. Create sync batch
        SyncBatch syncBatch = new SyncBatch();

        syncBatch.setDevice(device);
        syncBatch.setStartedAt(
                LocalDateTime.now()
        );
        syncBatch.setTransactionCount(
                request.transactions().size()
        );
        syncBatch.setSuccessCount(0);
        syncBatch.setRejectedCount(0);
        syncBatch.setStatus(
                SyncBatchStatus.PROCESSING
        );

        SyncBatch savedBatch =
                syncBatchRepository.save(syncBatch);

        // 4. Update device last seen
        device.setLastSeenAt(
                LocalDateTime.now()
        );

        deviceRepository.save(device);

        // 5. Process every transaction
        List<TransactionSyncResult> results =
                new ArrayList<>();

        int successCount = 0;
        int rejectedCount = 0;

        for (TransactionSyncItem item :
                request.transactions()) {

            TransactionSyncResult result;

            try {

                result = processTransaction(
                        item,
                        device
                );

                if (result.status() ==
                        TransactionStatus.SETTLED) {

                    successCount++;

                } else {

                    rejectedCount++;
                }

            } catch (Exception e) {

                rejectedCount++;

                result = new TransactionSyncResult(
                        item.txId(),
                        TransactionStatus.REJECTED,
                        determineRejectionReason(e),
                        null
                );
            }

            results.add(result);
        }

        // 6. Complete sync batch
        savedBatch.setCompletedAt(
                LocalDateTime.now()
        );

        savedBatch.setSuccessCount(
                successCount
        );

        savedBatch.setRejectedCount(
                rejectedCount
        );

        if (rejectedCount == 0) {

            savedBatch.setStatus(
                    SyncBatchStatus.COMPLETED
            );

        } else if (successCount == 0) {

            savedBatch.setStatus(
                    SyncBatchStatus.PARTIAL
            );

        } else {

            savedBatch.setStatus(
                    SyncBatchStatus.PARTIAL
            );
        }

        syncBatchRepository.save(savedBatch);

        // 7. Return complete sync response
        return new SyncTransactionsResponse(
                savedBatch.getId(),
                results
        );
    }

    private TransactionSyncResult processTransaction(
            TransactionSyncItem item,
            Device device
    ) {

        // Basic request validation
        validateSyncItem(item);

        /*
         * Make sure the device submitting the sync is
         * the same device that originally created the
         * transaction.
         */
        if (!device.getId().equals(
                item.senderDeviceId()
        )) {

            return new TransactionSyncResult(
                    item.txId(),
                    TransactionStatus.REJECTED,
                    RejectionReason.INVALID_DEVICE,
                    null
            );
        }

        /*
         * Check whether transaction already exists.
         *
         * This also gives us idempotent behaviour when
         * the same sync request is accidentally retried.
         */
        if (transactionRepository.existsById(
                item.txId()
        )) {

            Transaction existing =
                    transactionRepository.findById(
                            item.txId()
                    ).orElseThrow();

            if (existing.getStatus() ==
                    TransactionStatus.SETTLED) {

                Settlement settlement =
                        settlementService.getSettlement(
                                existing.getId()
                        );

                return new TransactionSyncResult(
                        existing.getId(),
                        TransactionStatus.SETTLED,
                        null,
                        settlement.getSettlementReference()
                );
            }

            return new TransactionSyncResult(
                    item.txId(),
                    TransactionStatus.REJECTED,
                    RejectionReason.DUPLICATE_NONCE,
                    null
            );
        }

        // Check nonce before creating transaction
        nonceService.validateNonce(
                item.nonce()
        );

        /*
         * Convert the sync item into the normal
         * transaction creation request.
         */
        CreateTransactionRequest transactionRequest =
                new CreateTransactionRequest(
                        item.txId(),
                        item.senderId(),
                        item.receiverId(),
                        item.senderDeviceId(),
                        item.amount(),
                        item.currency(),
                        item.nonce(),
                        item.timestamp(),
                        item.signature()
                );

        // Create transaction
        TransactionResponse transactionResponse =
                transactionService.createTransaction(
                        transactionRequest
                );

        /*
         * Retrieve the persisted entity because
         * SignatureService works on Transaction.
         */
        Transaction transaction =
                transactionRepository.findById(
                        transactionResponse.txId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Transaction could not be loaded"
                        )
                );

        // Verify ECDSA signature
        boolean signatureValid =
                signatureService
                        .verifyTransactionSignature(
                                transaction
                        );

        if (!signatureValid) {

            transaction.setStatus(
                    TransactionStatus.REJECTED
            );

            transaction.setRejectionReason(
                    RejectionReason.INVALID_SIGNATURE
            );

            transactionRepository.save(transaction);

            return new TransactionSyncResult(
                    transaction.getId(),
                    TransactionStatus.REJECTED,
                    RejectionReason.INVALID_SIGNATURE,
                    null
            );
        }

        // Validate transaction age
        validateTransactionAge(
                transaction.getClientTimestamp()
        );

        // Set validation state
        transaction.setStatus(
                TransactionStatus.VALIDATING
        );

        transactionRepository.save(transaction);

        // Finally settle transaction
        Settlement settlement =
                settlementService.settleTransaction(
                        transaction.getId()
                );

        return new TransactionSyncResult(
                transaction.getId(),
                TransactionStatus.SETTLED,
                null,
                settlement.getSettlementReference()
        );
    }

    private void validateSyncItem(
            TransactionSyncItem item
    ) {

        if (item == null) {
            throw new RuntimeException(
                    "Transaction sync item is required"
            );
        }

        if (item.txId() == null) {
            throw new RuntimeException(
                    "Transaction ID is required"
            );
        }

        if (item.senderId() == null) {
            throw new RuntimeException(
                    "Sender ID is required"
            );
        }

        if (item.receiverId() == null) {
            throw new RuntimeException(
                    "Receiver ID is required"
            );
        }

        if (item.nonce() == null ||
                item.nonce().isBlank()) {

            throw new RuntimeException(
                    "Nonce is required"
            );
        }

        if (item.signature() == null ||
                item.signature().isBlank()) {

            throw new RuntimeException(
                    "Signature is required"
            );
        }

        if (item.timestamp() == null) {
            throw new RuntimeException(
                    "Timestamp is required"
            );
        }
    }

    private void validateTransactionAge(
            Long timestamp
    ) {

        long currentTime =
                System.currentTimeMillis();

        /*
         * TapPay currently accepts transactions up to
         * 24 hours old.
         *
         * This is a demo/hackathon policy and can later
         * be moved to configuration.
         */
        long maxAge =
                24L * 60 * 60 * 1000;

        if (timestamp < currentTime - maxAge) {

            throw new RuntimeException(
                    "Transaction has expired"
            );
        }

        // Prevent future-dated transactions
        long allowedFutureDifference =
                5L * 60 * 1000;

        if (timestamp >
                currentTime + allowedFutureDifference) {

            throw new RuntimeException(
                    "Transaction timestamp is invalid"
            );
        }
    }

    private RejectionReason determineRejectionReason(
            Exception exception
    ) {

        String message = exception.getMessage();

        if (message == null) {
            return RejectionReason.INVALID_AMOUNT;
        }

        if (message.contains(
                "Nonce already used"
        )) {
            return RejectionReason.DUPLICATE_NONCE;
        }

        if (message.contains(
                "Invalid credentials"
        )) {
            return RejectionReason.USER_INACTIVE;
        }

        if (message.contains(
                "Device"
        )) {
            return RejectionReason.INVALID_DEVICE;
        }

        if (message.contains(
                "expired"
        )) {
            return RejectionReason.TRANSACTION_EXPIRED;
        }

        if (message.contains(
                "timestamp"
        )) {
            return RejectionReason.INVALID_TIMESTAMP;
        }

        if (message.contains(
                "Insufficient"
        )) {
            return RejectionReason.INSUFFICIENT_BALANCE;
        }

        if (message.contains(
                "amount"
        )) {
            return RejectionReason.INVALID_AMOUNT;
        }

        return RejectionReason.INVALID_AMOUNT;
    }
}