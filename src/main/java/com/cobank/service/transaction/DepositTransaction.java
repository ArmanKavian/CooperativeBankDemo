package com.cobank.service.transaction;

import com.cobank.domain.Account;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DepositTransaction implements AccountTransaction {

    private final Account account;
    private final double amount;

    @Override
    public void applyTransaction() {
        final double newAmount = account.getBalance() + amount;
        account.setBalance(newAmount);
    }
}
