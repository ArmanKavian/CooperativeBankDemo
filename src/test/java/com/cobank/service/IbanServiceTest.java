package com.cobank.service;


import com.cobank.service.iban.IbanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
class IbanServiceTest {

    @Autowired
    private IbanService ibanService;

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

