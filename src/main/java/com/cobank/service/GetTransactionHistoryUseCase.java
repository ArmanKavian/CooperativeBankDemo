package com.cobank.service;

import com.cobank.api.dto.TransactionHistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetTransactionHistoryUseCase {
    Page<TransactionHistoryResponse> getTransactionHistory(String iban, Pageable pageable);
}
