CREATE TABLE transaction_history
(
    id          UUID PRIMARY KEY,
    iban        VARCHAR(34)    NOT NULL,
    amount      DECIMAL(15, 2) NOT NULL,
    transaction_type        VARCHAR(20)    NOT NULL,
    timestamp   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    description TEXT,
    resulting_balance DECIMAL(15, 2) NOT NULL
);

CREATE INDEX idx_iban_timestamp ON transaction_history (iban, timestamp DESC);