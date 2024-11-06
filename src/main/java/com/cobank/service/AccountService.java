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
    private final IbanService ibanService;

    public AccountService(AccountRepository accountRepository, IbanService ibanService) {
        this.accountRepository = accountRepository;
        this.ibanService = ibanService;
    }

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

    private Function<Account, CreateAccountResponse> toCreateAccountResponse() {
        return account -> new CreateAccountResponse(
                account.getId(),
                account.getIban(),
                account.getAddress()
        );
    }
}

