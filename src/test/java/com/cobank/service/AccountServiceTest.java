package com.cobank.service;

import com.cobank.api.dto.CreateAccountRequest;
import com.cobank.api.dto.CreateAccountResponse;
import com.cobank.api.dto.FetchBalanceResponse;
import com.cobank.domain.Account;
import com.cobank.repository.AccountRepository;
import com.cobank.service.iban.IbanService;
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

    @Mock
    private IbanService ibanService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createAccount_ShouldReturnCreateAccountResponse_WhenValidRequest() {
        // Arrange
        CreateAccountRequest request = new CreateAccountRequest("Arman", "Beethovenstraat 7", "arman@mymail.com");

        String generatedIban = "NL00COOP1234567890";
        when(ibanService.generateIban()).thenReturn(generatedIban);


        Account mockAccount = Account.builder()
                .id(UUID.randomUUID())
                .iban(generatedIban)
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

    @Test
    void getBalanceByIban_ShouldReturnFetchBalanceResponse_WhenAccountExists() {
        // Arrange
        String iban = "NL00COOP1234567890";
        Account mockAccount = Account.builder()
                .id(UUID.randomUUID())
                .iban(iban)
                .balance(501.0)
                .build();

        when(accountRepository.findByIban(iban)).thenReturn(Optional.of(mockAccount));

        // Act
        Optional<FetchBalanceResponse> balanceResponse = accountService.getBalanceByIban(iban);

        // Assert
        assertTrue(balanceResponse.isPresent());
        assertEquals(iban, balanceResponse.get().iban());
        assertEquals(501.0, balanceResponse.get().balance());
        verify(accountRepository, times(1)).findByIban(iban);
    }

    @Test
    void getBalanceByIban_ShouldReturnEmpty_WhenAccountDoesNotExist() {
        // Arrange
        String iban = "NL00COOP1234567890";
        when(accountRepository.findByIban(iban)).thenReturn(Optional.empty());

        // Act
        Optional<FetchBalanceResponse> balanceResponse = accountService.getBalanceByIban(iban);

        // Assert
        assertFalse(balanceResponse.isPresent());
        verify(accountRepository, times(1)).findByIban(iban);
    }
}
