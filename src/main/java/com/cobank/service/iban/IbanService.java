package com.cobank.service.iban;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.stream.Collectors;

@Service
public class IbanService {
    private final String countryCode;
    private final String checkDigits;
    private final String bankCode;
    private final int accountNumberLength;
    private final SecureRandom random = new SecureRandom();

    public IbanService(
            @Value("${iban.country-code}") String countryCode,
            @Value("${iban.check-digits}") String checkDigits,
            @Value("${iban.bank-code}") String bankCode,
            @Value("${iban.account-number-length}") int accountNumberLength
    ) {
        this.countryCode = countryCode;
        this.checkDigits = checkDigits;
        this.bankCode = bankCode;
        this.accountNumberLength = accountNumberLength;
    }

    public String generateIban() {
        String accountNumber = generateRandomNumericString();
        return "%s%s%s%s".formatted(countryCode, checkDigits, bankCode, accountNumber);
    }

    private String generateRandomNumericString() {
        return random.ints(accountNumberLength, 0, 10)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining());
    }
}