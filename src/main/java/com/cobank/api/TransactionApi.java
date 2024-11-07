package com.cobank.api;

import com.cobank.api.dto.TransactionRequest;
import com.cobank.api.dto.TransactionResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;

public interface TransactionApi {

    @PostMapping("/transactions")
    ResponseEntity<TransactionResponse> processTransaction(@Valid @RequestBody TransactionRequest request);
}