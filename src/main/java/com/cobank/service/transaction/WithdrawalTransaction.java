package com.cobank.service.transaction;


import com.cobank.domain.Account;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class WithdrawalTransaction implements AccountTransaction {

    private final Account account;
    private final double amount;

    @Override
    public void validate() {
        AccountTransaction.super.validate();
        if (account.getBalance() < amount) {
            throw new IllegalArgumentException("Insufficient funds for withdrawal.");
        }
    }

    @Override
    public void applyTransaction() {
        account.setBalance(account.getBalance() - amount);
    }
}
