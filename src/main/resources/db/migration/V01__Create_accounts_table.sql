CREATE TABLE accounts
(
    id         UUID PRIMARY KEY,
    iban       VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    address    VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    balance DOUBLE NOT NULL
);