CREATE TABLE IF NOT EXISTS transfers (
    id BIGSERIAL PRIMARY KEY,
    from_card_id BIGINT NOT NULL,
    to_card_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    from_card_new_balance DECIMAL(15,2) NOT NULL,
    to_card_new_balance DECIMAL(15,2) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_transfers_from_card FOREIGN KEY (from_card_id)
        REFERENCES cards(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_transfers_to_card FOREIGN KEY (to_card_id)
        REFERENCES cards(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);