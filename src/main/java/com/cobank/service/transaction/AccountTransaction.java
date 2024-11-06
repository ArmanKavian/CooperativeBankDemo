package com.cobank.service.transaction;


import com.cobank.domain.Account;

public interface AccountTransaction {

    Account getAccount();
    double getAmount();

    // template method pattern for specifying steps of the algorithm
    default void execute() {
        validate();
        applyTransaction();
        postTransaction();
    }

    default void validate() {
        if (getAmount() <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }
    }

    void applyTransaction();

    // Optional hook
    default void postTransaction() {
        // No-op by default
    }
}
