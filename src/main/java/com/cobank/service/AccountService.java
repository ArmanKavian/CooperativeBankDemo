package com.cobank.service;


import com.cobank.api.dto.CreateAccountRequest;
import com.cobank.api.dto.CreateAccountResponse;
import com.cobank.domain.Account;
import com.cobank.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Service
public class AccountService implements CreateAccountUseCase {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Optional<CreateAccountResponse> createAccount(CreateAccountRequest request) {
        String iban = "NL" + (int) (Math.random() * 1_000_000_000L);

        Account account = Account.builder()
                .id(UUID.randomUUID())
                .iban(iban)
                .firstName(request.firstName())
                .address(request.address())
                .email(request.email())
                .balance(0.0) // Initial balance
                .build();

        return Optional.ofNullable(accountRepository.save(account))
                .map(toCreateAccountResponse());
    }

    private Function<Account, CreateAccountResponse> toCreateAccountResponse() {
        return account -> new CreateAccountResponse(
                account.getId(),
                account.getIban(),
                account.getAddress()
        );
    }
}

