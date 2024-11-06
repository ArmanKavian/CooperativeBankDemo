package com.cobank.api;

import com.cobank.api.dto.CreateAccountRequest;
import com.cobank.api.dto.CreateAccountResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Optional;

public interface AccountApi {

    @PostMapping("/accounts")
    ResponseEntity<Optional<CreateAccountResponse>> createAccount(@Valid @RequestBody CreateAccountRequest request);
}
