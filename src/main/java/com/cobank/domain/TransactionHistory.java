package com.cobank.domain;

import com.cobank.api.dto.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transaction_history", indexes = {
        @Index(name = "idx_iban_timestamp", columnList = "iban, timestamp DESC")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String iban;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private double resultingBalance;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(length = 255)
    private String description = "";
}