package com.cobank.web;

import com.cobank.api.AccountApi;
import com.cobank.api.dto.CreateAccountRequest;
import com.cobank.api.dto.CreateAccountResponse;
import com.cobank.service.CreateAccountUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class AccountController implements AccountApi {

    private final CreateAccountUseCase createAccountUseCase;

    public AccountController(CreateAccountUseCase createAccountUseCase) {
        this.createAccountUseCase = createAccountUseCase;
    }

    @Override
    public ResponseEntity<Optional<CreateAccountResponse>> createAccount(CreateAccountRequest request) {
        return createAccountUseCase.createAccount(request)
                .map(response -> new ResponseEntity<>(Optional.of(response), HttpStatus.CREATED))
                .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
