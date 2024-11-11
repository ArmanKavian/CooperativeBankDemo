package com.cobank.service;

import com.cobank.service.iban.IbanService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class IbanServiceTest {

    @Autowired
    private IbanService ibanService;

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("cobankdb_test")
            .withUsername("postgres")
            .withPassword("postgres");

    @BeforeAll
    public static void init() {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureTestDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void generateIban_ShouldReturnValidIbanFormat() {
        String iban = ibanService.generateIban();

        assertNotNull(iban);
        assertTrue(iban.startsWith("NL00COOP"));
        assertEquals(18, iban.length());
    }

    @Test
    void generateIban_ShouldGenerateUniqueIbans() {
        String iban1 = ibanService.generateIban();
        String iban2 = ibanService.generateIban();

        assertNotEquals(iban1, iban2, "Each generated IBAN should be unique.");
    }
}