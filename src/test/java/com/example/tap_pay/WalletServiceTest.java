package com.example.tap_pay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WalletServiceImpl walletService;


    @Test
    void createWallet_shouldCreateWalletSuccessfully() {

        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setName("Khushi");
        user.setPhoneNumber("9876543210");
        user.setActive(true);
        user.setRole(UserRole.CUSTOMER);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        Wallet savedWallet = createWallet(
                user,
                new BigDecimal("0.00"),
                new BigDecimal("500.00"),
                new BigDecimal("0.00")
        );

        when(walletRepository.save(any(Wallet.class)))
                .thenReturn(savedWallet);

        WalletResponse response =
                walletService.createWallet(userId);

        assertNotNull(response);

        assertEquals(userId, response.userId());
        assertEquals(
                new BigDecimal("0.00"),
                response.availableBalance()
        );
        assertEquals(
                new BigDecimal("500.00"),
                response.offlineLimit()
        );
        assertEquals(
                new BigDecimal("0.00"),
                response.offlineUsed()
        );

        verify(userRepository).findById(userId);
        verify(walletRepository).findByUserId(userId);
        verify(walletRepository).save(any(Wallet.class));
    }


    @Test
    void createWallet_shouldRejectInactiveUser() {

        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setName("Khushi");
        user.setPhoneNumber("9876543210");
        user.setActive(false);
        user.setRole(UserRole.CUSTOMER);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> walletService.createWallet(userId)
        );

        assertEquals(
                "Cannot create wallet for inactive user",
                exception.getMessage()
        );

        verify(walletRepository, never())
                .findByUserId(any());

        verify(walletRepository, never())
                .save(any(Wallet.class));
    }


    @Test
    void createWallet_shouldRejectDuplicateWallet() {

        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setActive(true);
        user.setRole(UserRole.CUSTOMER);

        Wallet existingWallet = createWallet(
                user,
                new BigDecimal("1000.00"),
                new BigDecimal("500.00"),
                new BigDecimal("0.00")
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.of(existingWallet));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> walletService.createWallet(userId)
        );

        assertEquals(
                "Wallet already exists for this user",
                exception.getMessage()
        );

        verify(walletRepository, never())
                .save(any(Wallet.class));
    }


    @Test
    void createWallet_shouldRejectUnknownUser() {

        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> walletService.createWallet(userId)
        );

        assertEquals(
                "User not found",
                exception.getMessage()
        );

        verify(walletRepository, never())
                .save(any(Wallet.class));
    }


    @Test
    void getWallet_shouldReturnWalletSuccessfully() {

        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setActive(true);

        Wallet wallet = createWallet(
                user,
                new BigDecimal("1000.00"),
                new BigDecimal("500.00"),
                new BigDecimal("100.00")
        );

        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.of(wallet));

        WalletResponse response =
                walletService.getWallet(userId);

        assertNotNull(response);

        assertEquals(userId, response.userId());

        assertEquals(
                new BigDecimal("1000.00"),
                response.availableBalance()
        );

        assertEquals(
                new BigDecimal("500.00"),
                response.offlineLimit()
        );

        assertEquals(
                new BigDecimal("100.00"),
                response.offlineUsed()
        );

        verify(walletRepository)
                .findByUserId(userId);
    }


    @Test
    void getWallet_shouldRejectMissingWallet() {

        UUID userId = UUID.randomUUID();

        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> walletService.getWallet(userId)
        );

        assertEquals(
                "Wallet not found",
                exception.getMessage()
        );
    }


    @Test
    void addBalance_shouldIncreaseBalanceSuccessfully() {

        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        Wallet wallet = createWallet(
                user,
                new BigDecimal("1000.00"),
                new BigDecimal("500.00"),
                new BigDecimal("0.00")
        );

        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.of(wallet));

        when(walletRepository.save(wallet))
                .thenReturn(wallet);

        WalletResponse response =
                walletService.addBalance(
                        userId,
                        new BigDecimal("500.00")
                );

        assertEquals(
                new BigDecimal("1500.00"),
                response.availableBalance()
        );

        verify(walletRepository).save(wallet);
    }


    @Test
    void addBalance_shouldRejectInvalidAmount() {

        UUID userId = UUID.randomUUID();

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> walletService.addBalance(
                        userId,
                        BigDecimal.ZERO
                )
        );

        assertEquals(
                "Amount must be greater than zero",
                exception.getMessage()
        );

        verify(walletRepository, never())
                .findByUserId(any());

        verify(walletRepository, never())
                .save(any(Wallet.class));
    }


    @Test
    void deductBalance_shouldDecreaseBalanceSuccessfully() {

        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        Wallet wallet = createWallet(
                user,
                new BigDecimal("1000.00"),
                new BigDecimal("500.00"),
                new BigDecimal("0.00")
        );

        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.of(wallet));

        when(walletRepository.save(wallet))
                .thenReturn(wallet);

        WalletResponse response =
                walletService.deductBalance(
                        userId,
                        new BigDecimal("300.00")
                );

        assertEquals(
                new BigDecimal("700.00"),
                response.availableBalance()
        );

        verify(walletRepository).save(wallet);
    }


    @Test
    void deductBalance_shouldRejectInsufficientBalance() {

        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        Wallet wallet = createWallet(
                user,
                new BigDecimal("200.00"),
                new BigDecimal("500.00"),
                new BigDecimal("0.00")
        );

        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.of(wallet));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> walletService.deductBalance(
                        userId,
                        new BigDecimal("300.00")
                )
        );

        assertEquals(
                "Insufficient wallet balance",
                exception.getMessage()
        );

        verify(walletRepository, never())
                .save(any(Wallet.class));
    }


    @Test
    void reserveOfflineAmount_shouldReserveSuccessfully() {

        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        Wallet wallet = createWallet(
                user,
                new BigDecimal("1000.00"),
                new BigDecimal("500.00"),
                new BigDecimal("100.00")
        );

        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.of(wallet));

        when(walletRepository.save(wallet))
                .thenReturn(wallet);

        WalletResponse response =
                walletService.reserveOfflineAmount(
                        userId,
                        new BigDecimal("200.00")
                );

        assertEquals(
                new BigDecimal("300.00"),
                response.offlineUsed()
        );

        assertEquals(
                new BigDecimal("1000.00"),
                response.availableBalance()
        );

        verify(walletRepository).save(wallet);
    }


    @Test
    void reserveOfflineAmount_shouldRejectOfflineLimitExceeded() {

        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        Wallet wallet = createWallet(
                user,
                new BigDecimal("1000.00"),
                new BigDecimal("500.00"),
                new BigDecimal("400.00")
        );

        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.of(wallet));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> walletService.reserveOfflineAmount(
                        userId,
                        new BigDecimal("200.00")
                )
        );

        assertEquals(
                "Offline transaction limit exceeded",
                exception.getMessage()
        );

        verify(walletRepository, never())
                .save(any(Wallet.class));
    }


    @Test
    void reserveOfflineAmount_shouldRejectInsufficientBalance() {

        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        Wallet wallet = createWallet(
                user,
                new BigDecimal("100.00"),
                new BigDecimal("500.00"),
                new BigDecimal("0.00")
        );

        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.of(wallet));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> walletService.reserveOfflineAmount(
                        userId,
                        new BigDecimal("200.00")
                )
        );

        assertEquals(
                "Insufficient wallet balance",
                exception.getMessage()
        );

        verify(walletRepository, never())
                .save(any(Wallet.class));
    }


    @Test
    void releaseOfflineAmount_shouldDecreaseOfflineUsedSuccessfully() {

        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        Wallet wallet = createWallet(
                user,
                new BigDecimal("1000.00"),
                new BigDecimal("500.00"),
                new BigDecimal("300.00")
        );

        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.of(wallet));

        when(walletRepository.save(wallet))
                .thenReturn(wallet);

        WalletResponse response =
                walletService.releaseOfflineAmount(
                        userId,
                        new BigDecimal("100.00")
                );

        assertEquals(
                new BigDecimal("200.00"),
                response.offlineUsed()
        );

        verify(walletRepository).save(wallet);
    }


    @Test
    void releaseOfflineAmount_shouldNeverMakeOfflineUsedNegative() {

        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        Wallet wallet = new Wallet();
        wallet.setId(UUID.randomUUID());
        wallet.setUser(user);
        wallet.setAvailableBalance(new BigDecimal("1000.00"));
        wallet.setOfflineLimit(new BigDecimal("500.00"));
        wallet.setOfflineUsed(new BigDecimal("50.00"));
        wallet.setUpdatedAt(LocalDateTime.now());

        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.of(wallet));

        when(walletRepository.save(any(Wallet.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WalletResponse response =
                walletService.releaseOfflineAmount(
                        userId,
                        new BigDecimal("100.00")
                );

        assertEquals(
                0,
                response.offlineUsed().compareTo(BigDecimal.ZERO)
        );

        verify(walletRepository).save(wallet);
    }

    @Test
    void reserveOfflineAmount_shouldRejectZeroAmount() {

        UUID userId = UUID.randomUUID();

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> walletService.reserveOfflineAmount(
                        userId,
                        BigDecimal.ZERO
                )
        );

        assertEquals(
                "Amount must be greater than zero",
                exception.getMessage()
        );

        verify(walletRepository, never())
                .findByUserId(any());
    }


    @Test
    void deductBalance_shouldRejectZeroAmount() {

        UUID userId = UUID.randomUUID();

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> walletService.deductBalance(
                        userId,
                        BigDecimal.ZERO
                )
        );

        assertEquals(
                "Amount must be greater than zero",
                exception.getMessage()
        );

        verify(walletRepository, never())
                .findByUserId(any());
    }


    private Wallet createWallet(
            User user,
            BigDecimal availableBalance,
            BigDecimal offlineLimit,
            BigDecimal offlineUsed
    ) {

        Wallet wallet = new Wallet();

        wallet.setId(UUID.randomUUID());
        wallet.setUser(user);
        wallet.setAvailableBalance(availableBalance);
        wallet.setOfflineLimit(offlineLimit);
        wallet.setOfflineUsed(offlineUsed);

        return wallet;
    }
}