package com.example.tap_pay;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    public WalletServiceImpl(
            WalletRepository walletRepository,
            UserRepository userRepository
    ) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public WalletResponse createWallet(UUID userId) {

        // Check user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        // User must be active
        if (!user.isActive()) {
            throw new RuntimeException(
                    "Cannot create wallet for inactive user"
            );
        }

        // Prevent duplicate wallet
        if (walletRepository.findByUserId(userId).isPresent()) {
            throw new RuntimeException(
                    "Wallet already exists for this user"
            );
        }

        // Create wallet
        Wallet wallet = new Wallet();

        wallet.setUser(user);

        wallet.setAvailableBalance(
                BigDecimal.ZERO
        );

        // Example offline limit for TapPay
        wallet.setOfflineLimit(
                new BigDecimal("500.00")
        );

        wallet.setOfflineUsed(
                BigDecimal.ZERO
        );

        wallet.setUpdatedAt(
                LocalDateTime.now()
        );

        Wallet savedWallet = walletRepository.save(wallet);

        return mapToResponse(savedWallet);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getWallet(UUID userId) {

        Wallet wallet = walletRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Wallet not found"
                        )
                );

        return mapToResponse(wallet);
    }

    @Override
    @Transactional
    public WalletResponse addBalance(
            UUID userId,
            BigDecimal amount
    ) {

        validateAmount(amount);

        Wallet wallet = getWalletEntity(userId);

        wallet.setAvailableBalance(
                wallet.getAvailableBalance().add(amount)
        );

        wallet.setUpdatedAt(
                LocalDateTime.now()
        );

        Wallet savedWallet = walletRepository.save(wallet);

        return mapToResponse(savedWallet);
    }

    @Override
    @Transactional
    public WalletResponse deductBalance(
            UUID userId,
            BigDecimal amount
    ) {

        validateAmount(amount);

        Wallet wallet = getWalletEntity(userId);

        // Check sufficient balance
        if (wallet.getAvailableBalance().compareTo(amount) < 0) {
            throw new RuntimeException(
                    "Insufficient wallet balance"
            );
        }

        wallet.setAvailableBalance(
                wallet.getAvailableBalance().subtract(amount)
        );

        wallet.setUpdatedAt(
                LocalDateTime.now()
        );

        Wallet savedWallet = walletRepository.save(wallet);

        return mapToResponse(savedWallet);
    }

    @Override
    @Transactional
    public WalletResponse reserveOfflineAmount(
            UUID userId,
            BigDecimal amount
    ) {

        validateAmount(amount);

        Wallet wallet = getWalletEntity(userId);

        /*
         * The amount reserved for offline transactions
         * cannot exceed the configured offline limit.
         */
        BigDecimal newOfflineUsed =
                wallet.getOfflineUsed().add(amount);

        if (newOfflineUsed.compareTo(
                wallet.getOfflineLimit()
        ) > 0) {

            throw new RuntimeException(
                    "Offline transaction limit exceeded"
            );
        }

        /*
         * Also make sure the user's wallet has enough
         * balance to cover the offline transaction.
         */
        if (wallet.getAvailableBalance().compareTo(amount) < 0) {
            throw new RuntimeException(
                    "Insufficient wallet balance"
            );
        }

        wallet.setOfflineUsed(newOfflineUsed);

        wallet.setUpdatedAt(
                LocalDateTime.now()
        );

        Wallet savedWallet = walletRepository.save(wallet);

        return mapToResponse(savedWallet);
    }

    @Override
    @Transactional
    public WalletResponse releaseOfflineAmount(
            UUID userId,
            BigDecimal amount
    ) {

        validateAmount(amount);

        Wallet wallet = getWalletEntity(userId);

        BigDecimal newOfflineUsed =
                wallet.getOfflineUsed().subtract(amount);

        /*
         * Never allow offlineUsed to become negative.
         */
        if (newOfflineUsed.compareTo(BigDecimal.ZERO) < 0) {
            newOfflineUsed = BigDecimal.ZERO;
        }

        wallet.setOfflineUsed(newOfflineUsed);

        wallet.setUpdatedAt(
                LocalDateTime.now()
        );

        Wallet savedWallet = walletRepository.save(wallet);

        return mapToResponse(savedWallet);
    }

    private Wallet getWalletEntity(UUID userId) {

        return walletRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Wallet not found"
                        )
                );
    }

    private void validateAmount(BigDecimal amount) {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new RuntimeException(
                    "Amount must be greater than zero"
            );
        }
    }

    private WalletResponse mapToResponse(Wallet wallet) {

        return new WalletResponse(
                wallet.getUser().getId(),
                wallet.getAvailableBalance(),
                wallet.getOfflineLimit(),
                wallet.getOfflineUsed()
        );
    }
}
