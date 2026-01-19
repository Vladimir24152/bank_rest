CREATE TABLE IF NOT EXISTS cards (
    id BIGSERIAL PRIMARY KEY,
    encrypted_card_number TEXT NOT NULL UNIQUE,
    last_four_digits VARCHAR(4) NOT NULL,
    user_id BIGINT NOT NULL,
    expiration_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'REQUEST_FOR_BLOCKING', 'BLOCKED', 'EXPIRED')),
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_card_user FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);