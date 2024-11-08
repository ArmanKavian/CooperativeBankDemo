package com.cobank.service.transaction;

import com.cobank.api.dto.TransactionHistoryResponse;
import com.cobank.api.dto.TransactionRequest;
import com.cobank.api.dto.TransactionResponse;
import com.cobank.api.dto.TransactionType;
import com.cobank.domain.Account;
import com.cobank.repository.AccountRepository;
import com.cobank.repository.TransactionHistoryRepository;
import jakarta.persistence.PessimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@EnableTransactionManagement
class TransactionServiceTest {

    @MockBean
    private AccountRepository accountRepository;

    @MockBean
    private TransactionHistoryRepository transactionHistoryRepository;

    @Autowired
    private TransactionService transactionService;

    private Account testAccount;
    private final String iban = "NL00COOP1234567890";
    private final double initialBalance = 1000.0;

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .id(UUID.randomUUID())
                .iban(iban)
                .firstName("Ludwig")
                .address("Beethovenstraat 9")
                .email("ludwig.beethoven@mymail.com")
                .balance(initialBalance)
                .build();

        when(transactionHistoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void processTransaction_ShouldProcessDepositSuccessfully() {
        TransactionRequest request = new TransactionRequest(iban, TransactionType.DEPOSIT, 100.0);
        when(accountRepository.findByIbanForUpdate(iban)).thenReturn(Optional.of(testAccount));

        Optional<TransactionResponse> response = transactionService.processTransaction(request);

        assertTrue(response.isPresent());
        assertEquals(iban, response.get().iban());
        assertEquals(initialBalance + 100.0, response.get().newBalance());
        assertEquals("Transaction processed successfully", response.get().description());
        verify(accountRepository, times(1)).save(testAccount);
        verify(transactionHistoryRepository, times(1)).save(any());
    }

    @Test
    void processTransaction_ShouldProcessWithdrawalSuccessfully() {
        TransactionRequest request = new TransactionRequest(iban, TransactionType.WITHDRAWAL, 100.0);
        when(accountRepository.findByIbanForUpdate(iban)).thenReturn(Optional.of(testAccount));

        Optional<TransactionResponse> response = transactionService.processTransaction(request);

        assertTrue(response.isPresent());
        assertEquals(iban, response.get().iban());
        assertEquals(initialBalance - 100.0, response.get().newBalance());
        assertEquals("Transaction processed successfully", response.get().description());
        verify(accountRepository, times(1)).save(testAccount);
        verify(transactionHistoryRepository, times(1)).save(any());
    }

    @Test
    void processTransaction_ShouldReturnError_WhenInvalidAmount() {
        TransactionRequest request = new TransactionRequest(iban, TransactionType.DEPOSIT, -100.0);

        Optional<TransactionResponse> response = transactionService.processTransaction(request);

        assertTrue(response.isPresent());
        assertEquals(iban, response.get().iban());
        assertEquals(-1, response.get().newBalance());
        assertEquals("Invalid transaction amount", response.get().description());
        verify(accountRepository, never()).findByIbanForUpdate(any());
    }

    @Test
    void processTransaction_ShouldRetryOnPessimisticLockException() {
        TransactionRequest request = new TransactionRequest(iban, TransactionType.DEPOSIT, 100.0);
        when(accountRepository.findByIbanForUpdate(iban))
                .thenThrow(new PessimisticLockException("Pessimistic lock failure"))
                .thenReturn(Optional.of(testAccount));

        Optional<TransactionResponse> response = transactionService.processTransaction(request);

        assertTrue(response.isPresent());
        assertEquals(initialBalance + 100.0, response.get().newBalance());
        verify(accountRepository, times(2)).findByIbanForUpdate(iban);
        verify(accountRepository, times(1)).save(testAccount);
    }

    @Test
    void processTransaction_ShouldInvokeRecoverMethod_WhenRetriesExhausted() {
        TransactionRequest request = new TransactionRequest(iban, TransactionType.WITHDRAWAL, 100.0);
        when(accountRepository.findByIbanForUpdate(iban))
                .thenThrow(new PessimisticLockException("Pessimistic lock failure"));

        Optional<TransactionResponse> response = transactionService.processTransaction(request);

        assertTrue(response.isPresent());
        assertEquals(-1, response.get().newBalance());
        assertEquals("Transaction could not be completed after multiple attempts. Please try again later.", response.get().description());
        verify(accountRepository, times(3)).findByIbanForUpdate(iban);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void processTransaction_ShouldHandleDataAccessException() {
        TransactionRequest request = new TransactionRequest(iban, TransactionType.DEPOSIT, 100.0);
        when(accountRepository.findByIbanForUpdate(iban))
                .thenThrow(new DataAccessException("Database error") {});

        Optional<TransactionResponse> response = transactionService.processTransaction(request);

        assertTrue(response.isPresent());
        assertEquals(-1, response.get().newBalance());
        assertEquals("Database error, please try again later", response.get().description());
        verify(accountRepository, times(1)).findByIbanForUpdate(iban);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void getTransactionHistory_ShouldReturnPagedTransactionHistory() {
        Pageable pageable = PageRequest.of(0, 5);
        when(transactionHistoryRepository.findByIbanOrderByTimestampDesc(eq(iban), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        Page<TransactionHistoryResponse> response = transactionService.getTransactionHistory(iban, pageable);

        assertNotNull(response);
        verify(transactionHistoryRepository, times(1)).findByIbanOrderByTimestampDesc(eq(iban), eq(pageable));
    }
}