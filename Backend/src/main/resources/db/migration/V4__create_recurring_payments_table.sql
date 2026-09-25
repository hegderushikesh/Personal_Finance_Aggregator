-- V4__create_recurring_payments_table.sql
-- Table definition and indexes for detected recurring payments

CREATE TABLE IF NOT EXISTS recurring_payments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    merchant_name VARCHAR(150) NOT NULL,
    normalized_merchant_name VARCHAR(150) NOT NULL,
    category_id BIGINT,
    account_id BIGINT,
    average_amount NUMERIC(19, 2) NOT NULL,
    last_amount NUMERIC(19, 2) NOT NULL,
    frequency VARCHAR(30) NOT NULL,
    interval_days INTEGER NOT NULL,
    next_expected_date DATE NOT NULL,
    last_transaction_date DATE NOT NULL,
    occurrence_count INTEGER NOT NULL,
    amount_variance NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    confidence INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT fk_recurring_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_recurring_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    CONSTRAINT fk_recurring_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_recurring_user_id ON recurring_payments(user_id);
CREATE INDEX IF NOT EXISTS idx_recurring_norm_merchant ON recurring_payments(user_id, normalized_merchant_name);
CREATE INDEX IF NOT EXISTS idx_recurring_status ON recurring_payments(status);
CREATE INDEX IF NOT EXISTS idx_recurring_next_date ON recurring_payments(next_expected_date);
