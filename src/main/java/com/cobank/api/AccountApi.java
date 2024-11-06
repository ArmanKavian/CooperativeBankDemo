package com.cobank.api;

import com.cobank.api.dto.CreateAccountRequest;
import com.cobank.api.dto.CreateAccountResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface AccountApi {

    @PostMapping("/accounts")
    ResponseEntity<CreateAccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request);
}
