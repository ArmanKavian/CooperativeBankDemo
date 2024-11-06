package com.cobank.service;


import com.cobank.api.dto.CreateAccountRequest;
import com.cobank.api.dto.CreateAccountResponse;

import java.util.Optional;

public interface CreateAccountUseCase {
    Optional<CreateAccountResponse> createAccount(CreateAccountRequest request);
}
