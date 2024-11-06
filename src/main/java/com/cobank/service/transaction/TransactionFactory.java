package com.cobank.service.transaction;

import com.cobank.api.dto.TransactionRequest;
import com.cobank.api.dto.TransactionType;
import com.cobank.domain.Account;

import java.util.Map;
import java.util.function.BiFunction;

public class TransactionFactory {

    private static final Map<TransactionType, BiFunction<Account, Double, AccountTransaction>> transactionMap = Map.of(
            TransactionType.DEPOSIT, DepositTransaction::new,
            TransactionType.WITHDRAWAL, WithdrawalTransaction::new
    );

    public static AccountTransaction createTransaction(Account account, TransactionRequest request) {
        return transactionMap.getOrDefault(request.type(),
                        (acc, amt) -> { throw new UnsupportedOperationException("Unsupported transaction type."); })
                .apply(account, request.amount());
    }
}
