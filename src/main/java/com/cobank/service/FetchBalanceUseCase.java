package com.cobank.service;

import com.cobank.api.dto.FetchBalanceResponse;

import java.util.Optional;

public interface FetchBalanceUseCase {
    Optional<FetchBalanceResponse> getBalanceByIban(String iban);
}
