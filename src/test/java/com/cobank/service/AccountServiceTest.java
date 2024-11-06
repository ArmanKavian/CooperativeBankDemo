package com.cobank.service;

import com.cobank.api.dto.CreateAccountRequest;
import com.cobank.api.dto.CreateAccountResponse;
import com.cobank.domain.Account;
import com.cobank.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    @InjectMocks
    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createAccount_ShouldReturnCreateAccountResponse_WhenValidRequest() {
        // Arrange
        CreateAccountRequest request = new CreateAccountRequest("Arman", "Beethovenstraat 7", "arman@mymail.com");

        Account mockAccount = Account.builder()
                .id(UUID.randomUUID())
                .iban("NL123456789")
                .firstName("Arman")
                .address("Beethovenstraat 7")
                .email("arman@mymail.com")
                .balance(0.0)
                .build();

        when(accountRepository.save(any(Account.class))).thenReturn(mockAccount);

        // Act
        Optional<CreateAccountResponse> response = accountService.createAccount(request);

        // Assert
        assertTrue(response.isPresent());
        assertEquals(mockAccount.getId(), response.get().id());
        assertEquals(mockAccount.getIban(), response.get().iban());
        assertEquals(mockAccount.getAddress(), response.get().address());

        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void createAccount_ShouldReturnEmptyOptional_WhenRepositoryFails() {
        // Arrange
        CreateAccountRequest request = new CreateAccountRequest(
                "Arman", "Beethovenstraat 7", "arman@mymail.com"
        );
        when(accountRepository.save(any(Account.class))).thenReturn(null);

        // Act
        Optional<CreateAccountResponse> response = accountService.createAccount(request);

        // Assert
        assertFalse(response.isPresent());

        verify(accountRepository, times(1)).save(any(Account.class));
    }
}
