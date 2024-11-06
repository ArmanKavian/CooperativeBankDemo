package com.cobank.service.iban;


import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.stream.Collectors;

@Service
public class IbanService {
    private static final String COUNTRY_CODE = "NL";
    private static final String CHECK_DIGITS = "00";
    private static final String BANK_CODE = "COOP";
    private static final int ACCOUNT_NUMBER_LENGTH = 10;
    private final SecureRandom random = new SecureRandom();

    public String generateIban() {
        String accountNumber = generateRandomNumericString();
        return "%s%s%s%s".formatted(COUNTRY_CODE, CHECK_DIGITS, BANK_CODE, accountNumber);
    }

    private String generateRandomNumericString() {
        return random.ints(ACCOUNT_NUMBER_LENGTH, 0, 10)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining());
    }
}

