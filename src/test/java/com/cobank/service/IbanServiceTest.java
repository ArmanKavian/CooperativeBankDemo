package com.cobank.service;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IbanServiceTest {

    private final IbanService ibanService = new IbanService();

    @Test
    void generateIban_ShouldReturnValidIbanFormat() {
        // Arrange
        String iban = ibanService.generateIban();

        // Assert
        assertNotNull(iban);
        assertTrue(iban.startsWith("NL00COOP"));
        assertEquals(18, iban.length()); // NL + 2 check digits + COOP + 10 digits for account number
    }

    @Test
    void generateIban_ShouldGenerateUniqueIbans() {
        // Arrange
        String iban1 = ibanService.generateIban();
        String iban2 = ibanService.generateIban();

        // Assert
        assertNotEquals(iban1, iban2, "Each generated IBAN should be unique.");
    }
}

