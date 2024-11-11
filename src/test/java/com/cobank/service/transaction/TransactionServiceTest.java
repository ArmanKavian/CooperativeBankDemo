package com.cobank.service.transaction;

import com.cobank.api.dto.TransactionHistoryResponse;
import com.cobank.api.dto.TransactionRequest;
import com.cobank.api.dto.TransactionResponse;
import com.cobank.api.dto.TransactionType;
import com.cobank.domain.Account;
import com.cobank.repository.AccountRepository;
import com.cobank.repository.TransactionHistoryRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EnableTransactionManagement
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Testcontainers
class TransactionServiceTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionHistoryRepository transactionHistoryRepository;

    @Autowired
    private TransactionService transactionService;

    private final String iban = "NL00COOP1234567890";
    private final BigDecimal initialBalance = BigDecimal.valueOf(1000.0);

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("cobankdb_test")
            .withUsername("postgres")
            .withPassword("postgres");

    @BeforeAll
    public static void init() {
        System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgres.getUsername());
        System.setProperty("spring.datasource.password", postgres.getPassword());
    }

    @BeforeEach
    void setUp() {
        transactionHistoryRepository.deleteAll();
        accountRepository.deleteAll();

        Account testAccount = Account.builder()
                .id(UUID.randomUUID())
                .iban(iban)
                .firstName("Ludwig")
                .address("Beethovenstraat 9")
                .email("ludwig.beethoven@mymail.com")
                .balance(initialBalance)
                .build();

        accountRepository.save(testAccount);
    }

    @Test
    void processTransaction_ShouldProcessDepositSuccessfully() {
        TransactionRequest request = new TransactionRequest(iban, TransactionType.DEPOSIT, BigDecimal.valueOf(100.0));

        Optional<TransactionResponse> response = transactionService.processTransaction(request);

        assertTrue(response.isPresent());
        assertEquals(iban, response.get().iban());
        assertEquals(0, initialBalance.add(BigDecimal.valueOf(100)).compareTo(response.get().newBalance()));
        assertEquals("Transaction processed successfully", response.get().description());

        assertEquals(1, transactionHistoryRepository.count());
        TransactionHistoryResponse transactionHistory = transactionService.getTransactionHistory(iban, PageRequest.of(0, 1))
                .getContent().get(0);
        assertEquals(TransactionType.DEPOSIT, transactionHistory.transactionType());
        assertEquals(0, BigDecimal.valueOf(100.0).compareTo(transactionHistory.amount()));
    }

    @Test
    void processTransaction_ShouldProcessWithdrawalSuccessfully() {
        TransactionRequest request = new TransactionRequest(iban, TransactionType.WITHDRAWAL, BigDecimal.valueOf(100.00));

        Optional<TransactionResponse> response = transactionService.processTransaction(request);

        assertTrue(response.isPresent());
        assertEquals(iban, response.get().iban());
        assertEquals(0, initialBalance.subtract(BigDecimal.valueOf(100.0)).compareTo(response.get().newBalance()));
        assertEquals("Transaction processed successfully", response.get().description());

        assertEquals(1, transactionHistoryRepository.count());
        TransactionHistoryResponse transactionHistory = transactionService.getTransactionHistory(iban, PageRequest.of(0, 1))
                .getContent().get(0);
        assertEquals(TransactionType.WITHDRAWAL, transactionHistory.transactionType());
        assertEquals(0, BigDecimal.valueOf(100.0).compareTo(transactionHistory.amount()));
    }

    @Test
    void processTransaction_ShouldReturnError_WhenInvalidAmount() {
        TransactionRequest request = new TransactionRequest(iban, TransactionType.DEPOSIT, BigDecimal.valueOf(-100.0));

        Optional<TransactionResponse> response = transactionService.processTransaction(request);

        assertTrue(response.isPresent());
        assertEquals(iban, response.get().iban());
        assertEquals(0, BigDecimal.valueOf(-1).compareTo(response.get().newBalance()));
        assertEquals("Invalid transaction amount", response.get().description());

        assertEquals(0, transactionHistoryRepository.count());
    }

    @Test
    void getTransactionHistory_ShouldReturnPagedTransactionHistory() {
        IntStream.range(0, 5)
                .mapToObj(i -> new TransactionRequest(iban, TransactionType.DEPOSIT, BigDecimal.valueOf(100.0 * (i + 1))))
                .forEach(transactionService::processTransaction);

        Pageable pageable = PageRequest.of(0, 3);
        Page<TransactionHistoryResponse> historyPage = transactionService.getTransactionHistory(iban, pageable);

        assertNotNull(historyPage);
        assertEquals(3, historyPage.getContent().size());
        assertEquals(5, historyPage.getTotalElements());
    }

    @Test
    @Transactional
    void shouldProcessConcurrentTransactions() throws ExecutionException, InterruptedException {
        TransactionRequest withdrawalRequest1 = new TransactionRequest(iban, TransactionType.WITHDRAWAL, BigDecimal.valueOf(100.0));
        TransactionRequest withdrawalRequest2 = new TransactionRequest(iban, TransactionType.WITHDRAWAL, BigDecimal.valueOf(150.0));

        CompletableFuture<Optional<TransactionResponse>> future1 = CompletableFuture.supplyAsync(() ->
                transactionService.processTransaction(withdrawalRequest1));
        CompletableFuture<Optional<TransactionResponse>> future2 = CompletableFuture.supplyAsync(() ->
                transactionService.processTransaction(withdrawalRequest2));

        Optional<TransactionResponse> response1 = future1.get();
        Optional<TransactionResponse> response2 = future2.get();

        assertTrue(response1.isPresent() && response2.isPresent());
        assertNotEquals(response1.get().newBalance(), response2.get().newBalance(),
                "Balances should differ");
    }
}