package com.cobank.web;

import com.cobank.api.dto.*;
import com.cobank.service.CreateAccountUseCase;
import com.cobank.service.FetchBalanceUseCase;
import com.cobank.service.ProcessTransactionUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
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

    @Value("${app.security.username}")
    private String username;

    @Value("${app.security.password}")
    private String password;

    private final TransactionRequest transactionRequest = new TransactionRequest(
            "NL00COOP1234567890", TransactionType.DEPOSIT, 100.0);

    private String basicAuthHeader() {
        return basicAuthHeader(username, password);
    }

    private String basicAuthHeader(String username, String password) {
        String auth = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());
    }

    @Test
    void createAccount_ShouldReturn201_WhenValidRequest() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest(
                "Ludwig Beethoven", "Beethovenstraat 9", "ludwig.beethoven@mymail.com"
        );

        CreateAccountResponse response = new CreateAccountResponse(
                UUID.randomUUID(),
                "NL123456789",
                "Beethovenstraat 9"
        );

        when(createAccountUseCase.createAccount(any(CreateAccountRequest.class))).thenReturn(Optional.of(response));

        mockMvc.perform(post("/accounts")
                        .header(HttpHeaders.AUTHORIZATION, basicAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.iban").value("NL123456789"))
                .andExpect(jsonPath("$.address").value("Beethovenstraat 9"));
    }

    @Test
    void createAccount_ShouldReturn400_WhenInvalidRequest() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest("", "", "invalid-email");

        mockMvc.perform(post("/accounts")
                        .header("Authorization", basicAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAccount_ShouldReturn500_WhenServiceFails() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest(
                "Ludwig", "Beethovenstraat 9", "ludwig.beethoven@mymail.com"
        );

        when(createAccountUseCase.createAccount(any(CreateAccountRequest.class))).thenReturn(Optional.empty());

        mockMvc.perform(post("/accounts")
                        .header("Authorization", basicAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getBalance_ShouldReturnFetchBalanceResponse_WhenAccountExists() throws Exception {
        String iban = "NL00COOP1234567890";
        double balance = 500.0;
        FetchBalanceResponse balanceResponse = new FetchBalanceResponse(iban, balance);

        when(fetchBalanceUseCase.getBalanceByIban(iban)).thenReturn(Optional.of(balanceResponse));

        mockMvc.perform(get("/accounts/balance/{iban}", iban)
                        .header("Authorization", basicAuthHeader())
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
                        .header("Authorization", basicAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void processTransaction_ShouldReturn200_WhenTransactionIsSuccessful() throws Exception {
        TransactionResponse response = new TransactionResponse("NL00COOP1234567890", 1100.0, "Transaction processed successfully");

        when(processTransactionUseCase.processTransaction(any(TransactionRequest.class))).thenReturn(Optional.of(response));

        mockMvc.perform(post("/transactions")
                        .header("Authorization", basicAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iban").value(transactionRequest.iban()))
                .andExpect(jsonPath("$.newBalance").value(1100.0))
                .andExpect(jsonPath("$.description").value("Transaction processed successfully"));
    }

    @Test
    void processTransaction_ShouldReturn500_WhenTransactionFails() throws Exception {
        when(processTransactionUseCase.processTransaction(any(TransactionRequest.class))).thenReturn(Optional.empty());

        mockMvc.perform(post("/transactions")
                        .header("Authorization", basicAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.iban").value(transactionRequest.iban()))
                .andExpect(jsonPath("$.newBalance").value(-1.0))
                .andExpect(jsonPath("$.description").value("Transaction failed"));
    }

    @Test
    void processTransaction_ShouldReturn400_WhenRequestIsInvalid() throws Exception {
        TransactionRequest invalidRequest = new TransactionRequest("NL00COOP1234567890", TransactionType.DEPOSIT, -100.0);

        mockMvc.perform(post("/transactions")
                        .header("Authorization", basicAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn401_WhenInvalidCredentialsProvided() throws Exception {
        mockMvc.perform(get("/accounts/balance/{iban}", "NL00COOP1234567890")
                        .header(HttpHeaders.AUTHORIZATION, basicAuthHeader("invalidUser", "invalidPassword"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}