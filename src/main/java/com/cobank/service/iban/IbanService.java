package com.cobank.service.iban;

import com.cobank.repository.AccountRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class IbanService {
    private final String countryCode;
    private final String checkDigits;
    private final String bankCode;
    private final int accountNumberLength;

    @PersistenceContext
    private EntityManager entityManager;

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
        long sequenceNumber = getNextAccountSequence();
        String accountNumber = String.format("%0" + accountNumberLength + "d", sequenceNumber);

        return "%s%s%s%s".formatted(countryCode, checkDigits, bankCode, accountNumber);
    }

    private long getNextAccountSequence() {
        Query query = entityManager.createNativeQuery(
                "SELECT NEXT VALUE FOR account_sequence"
        );
        return ((Number) query.getSingleResult()).longValue();
    }
}