package com.cobank.service.transaction;

import com.cobank.api.dto.TransactionHistoryResponse;
import com.cobank.api.dto.TransactionRequest;
import com.cobank.api.dto.TransactionResponse;
import com.cobank.api.dto.TransactionType;
import com.cobank.domain.Account;
import com.cobank.domain.TransactionHistory;
import com.cobank.repository.AccountRepository;
import com.cobank.repository.TransactionHistoryRepository;
import com.cobank.service.GetTransactionHistoryUseCase;
import com.cobank.service.ProcessTransactionUseCase;
import jakarta.persistence.PessimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService implements
        ProcessTransactionUseCase,
        GetTransactionHistoryUseCase {

    private final AccountRepository accountRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;

    @Override
    @Retryable(
            retryFor = {CannotAcquireLockException.class, PessimisticLockException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2) // Exponential backoff
    )
    @Transactional(isolation = Isolation.SERIALIZABLE, timeout = 5)
    public Optional<TransactionResponse> processTransaction(TransactionRequest request) {
        log.info("Starting transaction process for IBAN={} with type={} and amount={}",
                request.iban(), request.type(), request.amount());

        try {
            validateRequestAmount(request);
            return accountRepository.findByIbanForUpdate(request.iban())
                    .map(account -> executeTransaction(request, account));
        } catch (IllegalArgumentException ex) {
            log.error("Transaction failed due to invalid input: IBAN={}, Error={}", request.iban(), ex.getMessage());
            return Optional.of(new TransactionResponse(request.iban(), -1, "Invalid transaction amount"));
        } catch (DataAccessException ex) {
            log.error("Database error during transaction processing for IBAN={}: {}", request.iban(), ex.getMessage(), ex);
            return Optional.of(new TransactionResponse(request.iban(), -1, "Database error, please try again later"));
        }
    }

    private TransactionResponse executeTransaction(TransactionRequest request, Account account) {
        log.info("Executing transaction for IBAN={}, Type={}, Amount={}", request.iban(), request.type(), request.amount());

        double initialBalance = account.getBalance();
        double newBalance = applyTransaction(request, account);

        recordTransactionHistory(account.getIban(), request.type(), request.amount(), newBalance,
                String.format("%s transaction of %.2f", request.type(), request.amount()));

        log.info("Transaction completed successfully. IBAN={}, Initial Balance={}, New Balance={}",
                account.getIban(), initialBalance, newBalance);

        return new TransactionResponse(account.getIban(), newBalance, "Transaction processed successfully");
    }

    private double applyTransaction(TransactionRequest request, Account account) {
        log.debug("Applying {} transaction for IBAN={} with amount={}", request.type(), account.getIban(), request.amount());

        if (request.type() == TransactionType.DEPOSIT) {
            account.setBalance(account.getBalance() + request.amount());
        } else if (request.type() == TransactionType.WITHDRAWAL) {
            if (account.getBalance() < request.amount()) {
                throw new IllegalArgumentException("Insufficient funds for withdrawal.");
            }
            account.setBalance(account.getBalance() - request.amount());
        }
        accountRepository.save(account);
        log.debug("Transaction applied successfully for IBAN={}. New balance={}", account.getIban(), account.getBalance());
        return account.getBalance();
    }

    private void validateRequestAmount(TransactionRequest request) {
        log.debug("Validating transaction amount for IBAN={} with amount={}", request.iban(), request.amount());

        if (request.amount() <= 0) {
            log.warn("Invalid transaction amount for IBAN={}: {}", request.iban(), request.amount());
            throw new IllegalArgumentException("Transaction amount must be positive.");
        }
    }

    @Recover
    public Optional<TransactionResponse> recoverFromLockFailure(Exception ex, TransactionRequest request) {
        log.error("Transaction failed after retries due to lock acquisition issues for IBAN={}, Type={}, Amount={}. Error: {}",
                request.iban(), request.type(), request.amount(), ex.getMessage(), ex);

        return Optional.of(new TransactionResponse(request.iban(), -1,
                "Transaction could not be completed after multiple attempts. Please try again later."));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
    public TransactionHistory recordTransactionHistory(String iban, TransactionType transactionType,
                                                       double amount, double resultingBalance, String description) {
        log.debug("Recording transaction history for IBAN={}, Type={}, Amount={}, New Balance={}",
                iban, transactionType, amount, resultingBalance);

        TransactionHistory history = TransactionHistory.builder()
                .iban(iban)
                .transactionType(transactionType)
                .amount(amount)
                .resultingBalance(resultingBalance)
                .timestamp(LocalDateTime.now())
                .description(description)
                .build();
        TransactionHistory savedHistory = transactionHistoryRepository.save(history);

        log.debug("Transaction history recorded successfully for IBAN={}, History ID={}", iban, savedHistory.getId());
        return savedHistory;
    }

    @Cacheable(value = "transactionHistory", key = "#iban + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    @Override
    public Page<TransactionHistoryResponse> getTransactionHistory(String iban, Pageable pageable) {
        log.info("Fetching transaction history for IBAN={} with page={} and size={}", iban, pageable.getPageNumber(), pageable.getPageSize());

        Page<TransactionHistory> historyPage = transactionHistoryRepository.findByIbanOrderByTimestampDesc(iban, pageable);

        log.debug("Transaction history fetched for IBAN={} with total records={}", iban, historyPage.getTotalElements());
        return historyPage.map(this::toTransactionHistoryResponse);
    }

    private TransactionHistoryResponse toTransactionHistoryResponse(TransactionHistory history) {
        return new TransactionHistoryResponse(
                history.getIban(),
                history.getTransactionType(),
                history.getAmount(),
                history.getResultingBalance(),
                history.getTimestamp(),
                history.getDescription()
        );
    }
}