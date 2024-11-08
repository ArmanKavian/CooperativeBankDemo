package com.cobank.service;


import com.cobank.api.dto.*;
import com.cobank.domain.Account;
import com.cobank.repository.AccountRepository;
import com.cobank.service.iban.IbanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class AccountService implements
        CreateAccountUseCase,
        FetchBalanceUseCase {

    private final AccountRepository accountRepository;
    private final IbanService ibanService;

    @Transactional(
            isolation = Isolation.REPEATABLE_READ,
            propagation = Propagation.REQUIRED,
            timeout = 15)
    @Override
    public Optional<CreateAccountResponse> createAccount(CreateAccountRequest request) {
        // Call iban service
        String iban = ibanService.generateIban();

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

    @Transactional(readOnly = true)
    @Override
    public Optional<FetchBalanceResponse> getBalanceByIban(String iban) {
        return Optional.ofNullable(iban)
                .flatMap(accountRepository::findByIban)
                .map(toFetchBalanceResponse());
    }

    private Function<Account, CreateAccountResponse> toCreateAccountResponse() {
        return account -> new CreateAccountResponse(
                account.getId(),
                account.getIban(),
                account.getAddress()
        );
    }

    private Function<Account, FetchBalanceResponse> toFetchBalanceResponse() {
        return account -> new FetchBalanceResponse(
                account.getIban(),
                account.getBalance()
        );
    }
}

