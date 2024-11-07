package com.cobank.web;

import com.cobank.api.dto.*;
import com.cobank.service.CreateAccountUseCase;
import com.cobank.service.FetchBalanceUseCase;
import com.cobank.service.ProcessTransactionUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateAccountUseCase createAccountUseCase;

    @MockBean
    private FetchBalanceUseCase fetchBalanceUseCase;

    @MockBean
    private ProcessTransactionUseCase processTransactionUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    private final TransactionRequest transactionRequest = new TransactionRequest(
            "NL00COOP1234567890", TransactionType.DEPOSIT, 100.0);

    @Test
    void createAccount_ShouldReturn201_WhenValidRequest() throws Exception {
        // Arrange
        CreateAccountRequest request = new CreateAccountRequest(
                "Ludwig Beethoven", "Beethovenstraat 9", "ludwig.beethoven@mymail.com"
        );

        CreateAccountResponse response = new CreateAccountResponse(
                UUID.randomUUID(),
                "NL123456789",
                "Beethovenstraat 9"
        );

        when(createAccountUseCase.createAccount(any(CreateAccountRequest.class))).thenReturn(Optional.of(response));

        // Act & Assert
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.iban").value("NL123456789"))
                .andExpect(jsonPath("$.address").value("Beethovenstraat 9"));
    }

    @Test
    void createAccount_ShouldReturn400_WhenInvalidRequest() throws Exception {
        // Arrange: Create a request with missing fields
        CreateAccountRequest request = new CreateAccountRequest("", "", "invalid-email");

        // Act & Assert
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAccount_ShouldReturn500_WhenServiceFails() throws Exception {
        // Arrange
        CreateAccountRequest request = new CreateAccountRequest(
                "Ludwig", "Beethovenstraat 9", "ludwig.beethoven@mymail.com"
        );

        when(createAccountUseCase.createAccount(any(CreateAccountRequest.class))).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getBalance_ShouldReturnFetchBalanceResponseDTO_WhenAccountExists() throws Exception {
        String iban = "NL00COOP1234567890";
        double balance = 500.0;
        FetchBalanceResponse balanceResponse = new FetchBalanceResponse(iban, balance);

        when(fetchBalanceUseCase.getBalanceByIban(iban)).thenReturn(Optional.of(balanceResponse));

        mockMvc.perform(get("/accounts/balance/{iban}", iban)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iban").value(iban))
                .andExpect(jsonPath("$.balance").value(balance));
    }

    @Test
    void getBalance_ShouldReturn404_WhenAccountDoesNotExist() throws Exception {
        String iban = "NL00COOP1234567890";

        when(fetchBalanceUseCase.getBalanceByIban(iban)).thenReturn(Optional.empty());

        mockMvc.perform(get("/accounts/balance/{iban}", iban)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void processTransaction_ShouldReturn200_WhenTransactionIsSuccessful() throws Exception {
        // Arrange
        TransactionResponse response = new TransactionResponse("NL00COOP1234567890", 1100.0, "Transaction processed successfully");
        when(processTransactionUseCase.processTransaction(any(TransactionRequest.class))).thenReturn(Optional.of(response));

        // Act & Assert
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iban").value(transactionRequest.iban()))
                .andExpect(jsonPath("$.newBalance").value(1100.0))
                .andExpect(jsonPath("$.description").value("Transaction processed successfully"));
    }

    @Test
    void processTransaction_ShouldReturn500_WhenTransactionFails() throws Exception {
        // Arrange
        when(processTransactionUseCase.processTransaction(any(TransactionRequest.class))).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.iban").value(transactionRequest.iban()))
                .andExpect(jsonPath("$.newBalance").value(-1.0))
                .andExpect(jsonPath("$.description").value("Transaction failed"));
    }

    @Test
    void processTransaction_ShouldReturn400_WhenRequestIsInvalid() throws Exception {
        // Arrange
        TransactionRequest invalidRequest = new TransactionRequest("NL00COOP1234567890", TransactionType.DEPOSIT, -100.0);

        // Act & Assert
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
