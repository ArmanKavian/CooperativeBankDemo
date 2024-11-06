package com.cobank.service;

import com.cobank.api.dto.TransactionRequest;
import com.cobank.api.dto.TransactionResponse;

import java.util.Optional;

public interface ProcessTransactionUseCase {
    Optional<TransactionResponse> processTransaction(TransactionRequest request);
}
