package com.cobank.service.transaction;


import com.cobank.api.dto.TransactionRequest;
import com.cobank.api.dto.TransactionResponse;
import com.cobank.domain.Account;
import com.cobank.repository.AccountRepository;
import com.cobank.service.ProcessTransactionUseCase;
import jakarta.persistence.PessimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
public class TransactionService implements ProcessTransactionUseCase {

    private final AccountRepository accountRepository;

    public TransactionService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    @Retryable(
            retryFor = {CannotAcquireLockException.class, PessimisticLockException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2) // Exponential backoff
    )
    @Transactional(isolation = Isolation.SERIALIZABLE, timeout = 5)
    public Optional<TransactionResponse> processTransaction(TransactionRequest request) throws IllegalArgumentException {
        try {
            validateRequestAmount(request);

            // Pessimistic locking
            return accountRepository.findByIbanForUpdate(request.iban())
                    .map(account -> execute(request, account));

        } catch (IllegalArgumentException ex) {
            log.error("Transaction failed due to invalid input: IBAN={}, Error={}", request.iban(), ex.getMessage());
            return Optional.of(new TransactionResponse(request.iban(), -1,
                    "Invalid transaction amount"));
        } catch (DataAccessException ex) {
            log.error("Database error during transaction processing for IBAN={}: {}",
                    request.iban(), ex.getMessage(), ex);
            return Optional.of(new TransactionResponse(request.iban(), -1,
                    "Database error, please try again later"));
        }
    }

    private static void validateRequestAmount(TransactionRequest request) throws IllegalArgumentException {
        if (request.amount() <= 0) {
            log.warn("Invalid transaction amount for IBAN={}: {}", request.iban(), request.amount());
            throw new IllegalArgumentException("Transaction amount must be positive.");
        }
    }

    private TransactionResponse execute(TransactionRequest request, Account account) {
        AccountTransaction transaction = TransactionFactory.createTransaction(account, request);
        transaction.execute();

        accountRepository.save(account);

        log.info("Transaction processed successfully. IBAN={}, New Balance={}, Type={}, Amount={}",
                account.getIban(), account.getBalance(), request.type(), request.amount());

        return new TransactionResponse(account.getIban(), account.getBalance(),
                "Transaction processed successfully");
    }

    @Recover
    public Optional<TransactionResponse> recoverFromLockFailure(Exception ex, TransactionRequest request) {
        log.error("Transaction failed after multiple retries due to lock acquisition issues. " +
                        "Transaction details: IBAN={}, Type={}, Amount={}. Error: {}",
                request.iban(), request.type(), request.amount(), ex.getMessage(), ex);

        // fallback
        return Optional.of(new TransactionResponse(
                request.iban(),
                -1,  // Indicates an error or unavailable balance
                "Transaction could not be completed after multiple attempts. Please try again later."));
    }
}
