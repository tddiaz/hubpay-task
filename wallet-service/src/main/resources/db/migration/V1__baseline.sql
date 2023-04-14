CREATE TABLE wallet
(
    id                    BIGINT                      NOT NULL,
    customer_id           BIGINT                      NOT NULL,
    balance_currency      VARCHAR(3)                  NOT NULL,
    balance_amount        DECIMAL(19, 4)              NOT NULL,
    balance_held_currency VARCHAR(3)                  NOT NULL,
    balance_held_amount   DECIMAL(19, 4)              NOT NULL,
    created_at            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_wallet_id_customer_id UNIQUE (id, customer_id)
);


CREATE TABLE transaction
(
    id                   BIGINT                      NOT NULL,
    reference_id         VARCHAR                     NOT NULL,
    wallet_id            BIGINT                      NOT NULL,
    transaction_currency VARCHAR(3)                  NOT NULL,
    transaction_amount   DECIMAL(19, 4)              NOT NULL,
    status               VARCHAR(10)                 NOT NULL,
    transaction_type     VARCHAR(20)                 NOT NULL,
    entry                VARCHAR(10)                 NOT NULL,
    created_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_transaction_wallet_id FOREIGN KEY (wallet_id) REFERENCES wallet (id),
    CONSTRAINT uq_transaction_reference_id UNIQUE (reference_id)
);