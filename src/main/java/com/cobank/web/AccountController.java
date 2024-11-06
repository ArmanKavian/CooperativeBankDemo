package com.cobank.web;

import com.cobank.api.AccountApi;
import com.cobank.api.BalanceApi;
import com.cobank.api.dto.CreateAccountRequest;
import com.cobank.api.dto.CreateAccountResponse;
import com.cobank.api.dto.FetchBalanceResponse;
import com.cobank.service.CreateAccountUseCase;
import com.cobank.service.FetchBalanceUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController implements
        AccountApi,
        BalanceApi {

    private final CreateAccountUseCase createAccountUseCase;
    private final FetchBalanceUseCase fetchBalanceUseCase;

    public AccountController(CreateAccountUseCase createAccountUseCase, FetchBalanceUseCase fetchBalanceUseCase) {
        this.createAccountUseCase = createAccountUseCase;
        this.fetchBalanceUseCase = fetchBalanceUseCase;
    }

    @Override
    public ResponseEntity<CreateAccountResponse> createAccount(CreateAccountRequest request) {
        return createAccountUseCase.createAccount(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Override
    public ResponseEntity<FetchBalanceResponse> getBalance(String iban) {
        return fetchBalanceUseCase.getBalanceByIban(iban)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
