package com.cobank.api;

import com.cobank.api.dto.FetchBalanceResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface BalanceApi {
    @GetMapping("/accounts/balance/{iban}")
    ResponseEntity<FetchBalanceResponse> getBalance(@PathVariable String iban);
}
