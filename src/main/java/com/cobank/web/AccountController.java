package com.cobank.web;

import com.cobank.api.AccountApi;
import com.cobank.api.BalanceApi;
import com.cobank.api.TransactionApi;
import com.cobank.api.dto.*;
import com.cobank.service.CreateAccountUseCase;
import com.cobank.service.FetchBalanceUseCase;
import com.cobank.service.GetTransactionHistoryUseCase;
import com.cobank.service.ProcessTransactionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
public class AccountController implements
        AccountApi,
        BalanceApi,
        TransactionApi {

    private final CreateAccountUseCase createAccountUseCase;
    private final FetchBalanceUseCase fetchBalanceUseCase;
    private final ProcessTransactionUseCase processTransactionUseCase;
    private final GetTransactionHistoryUseCase getTransactionHistoryUseCase;

    @Override
    public ResponseEntity<CreateAccountResponse> createAccount(CreateAccountRequest request) {
        return createAccountUseCase.createAccount(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .orElseThrow(() -> new IllegalStateException("Failed to create account"));
    }

    @Override
    public ResponseEntity<FetchBalanceResponse> getBalance(String iban) {
        return fetchBalanceUseCase.getBalanceByIban(iban)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new FetchBalanceResponse(iban, BigDecimal.valueOf(-1.0))));
    }

    @Override
    public ResponseEntity<TransactionResponse> processTransaction(TransactionRequest request) {
        return processTransactionUseCase.processTransaction(request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.internalServerError().body(
                        new TransactionResponse(request.iban(), BigDecimal.valueOf(-1.0), "Transaction failed")));
    }

    @Override
    public ResponseEntity<Page<TransactionHistoryResponse>> getTransactionHistory(String iban, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionHistoryResponse> historyPage =
                getTransactionHistoryUseCase.getTransactionHistory(iban, pageable);
        if (historyPage.isEmpty()) {
            throw new IllegalArgumentException("No transaction history found for the specified IBAN");
        }
        return ResponseEntity.ok(historyPage);
    }
}