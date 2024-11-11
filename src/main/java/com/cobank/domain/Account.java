package com.cobank.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 34)
    private String iban;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Setter
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;
}