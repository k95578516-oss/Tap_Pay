package com.example.tap_pay;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NonceServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    private NonceServiceImpl nonceService;

    @BeforeEach
    void setUp() {
        nonceService = new NonceServiceImpl(
                transactionRepository
        );
    }

    // ---------------------------------------------------------
    // isNonceUsed()
    // ---------------------------------------------------------

    @Test
    void isNonceUsed_shouldReturnTrueWhenNonceExists() {

        String nonce = "nonce-123";

        when(transactionRepository.existsByNonce(nonce))
                .thenReturn(true);

        boolean result =
                nonceService.isNonceUsed(nonce);

        assertTrue(result);

        verify(transactionRepository)
                .existsByNonce(nonce);
    }

    @Test
    void isNonceUsed_shouldReturnFalseWhenNonceDoesNotExist() {

        String nonce = "nonce-123";

        when(transactionRepository.existsByNonce(nonce))
                .thenReturn(false);

        boolean result =
                nonceService.isNonceUsed(nonce);

        assertFalse(result);

        verify(transactionRepository)
                .existsByNonce(nonce);
    }

    @Test
    void isNonceUsed_shouldReturnFalseForNullNonce() {

        boolean result =
                nonceService.isNonceUsed(null);

        assertFalse(result);

        verify(transactionRepository, never())
                .existsByNonce(any());
    }

    @Test
    void isNonceUsed_shouldReturnFalseForBlankNonce() {

        boolean result =
                nonceService.isNonceUsed("   ");

        assertFalse(result);

        verify(transactionRepository, never())
                .existsByNonce(any());
    }

    @Test
    void isNonceUsed_shouldReturnFalseForEmptyNonce() {

        boolean result =
                nonceService.isNonceUsed("");

        assertFalse(result);

        verify(transactionRepository, never())
                .existsByNonce(any());
    }

    // ---------------------------------------------------------
    // validateNonce()
    // ---------------------------------------------------------

    @Test
    void validateNonce_shouldPassForNewNonce() {

        String nonce = "new-nonce";

        when(transactionRepository.existsByNonce(nonce))
                .thenReturn(false);

        assertDoesNotThrow(() ->
                nonceService.validateNonce(nonce)
        );

        verify(transactionRepository)
                .existsByNonce(nonce);
    }

    @Test
    void validateNonce_shouldRejectAlreadyUsedNonce() {

        String nonce = "used-nonce";

        when(transactionRepository.existsByNonce(nonce))
                .thenReturn(true);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> nonceService.validateNonce(nonce)
                );

        assertEquals(
                "Nonce already used",
                exception.getMessage()
        );

        verify(transactionRepository)
                .existsByNonce(nonce);
    }

    @Test
    void validateNonce_shouldRejectNullNonce() {

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> nonceService.validateNonce(null)
                );

        assertEquals(
                "Nonce is required",
                exception.getMessage()
        );

        verify(transactionRepository, never())
                .existsByNonce(any());
    }

    @Test
    void validateNonce_shouldRejectBlankNonce() {

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> nonceService.validateNonce("   ")
                );

        assertEquals(
                "Nonce is required",
                exception.getMessage()
        );

        verify(transactionRepository, never())
                .existsByNonce(any());
    }

    @Test
    void validateNonce_shouldRejectEmptyNonce() {

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> nonceService.validateNonce("")
                );

        assertEquals(
                "Nonce is required",
                exception.getMessage()
        );

        verify(transactionRepository, never())
                .existsByNonce(any());
    }

    @Test
    void validateNonce_shouldCheckRepositoryOnlyOnceForValidNonce() {

        String nonce = "unique-nonce";

        when(transactionRepository.existsByNonce(nonce))
                .thenReturn(false);

        nonceService.validateNonce(nonce);

        verify(
                transactionRepository,
                times(1)
        ).existsByNonce(nonce);
    }

    @Test
    void validateNonce_shouldNotCheckRepositoryWhenNonceIsInvalid() {

        assertThrows(
                RuntimeException.class,
                () -> nonceService.validateNonce(" ")
        );

        verify(
                transactionRepository,
                never()
        ).existsByNonce(any());
    }
}