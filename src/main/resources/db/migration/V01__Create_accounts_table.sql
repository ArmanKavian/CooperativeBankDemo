CREATE TABLE accounts
(
    id         UUID PRIMARY KEY,
    iban       VARCHAR(34)     NOT NULL UNIQUE,
    first_name VARCHAR(50)     NOT NULL,
    address    VARCHAR(255)     NOT NULL,
    email      VARCHAR(255)     NOT NULL UNIQUE,
    balance    NUMERIC(15, 2) NOT NULL
);
