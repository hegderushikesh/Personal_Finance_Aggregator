-- V3__add_plaid_and_source_columns.sql
-- Add source, external_account_id, plaid_connection_id to accounts
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS source VARCHAR(20) NOT NULL DEFAULT 'MANUAL';
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS external_account_id VARCHAR(100);
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS plaid_connection_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_accounts_ext_id ON accounts(external_account_id);
CREATE INDEX IF NOT EXISTS idx_accounts_plaid_conn ON accounts(plaid_connection_id);

-- Add source, external_transaction_id, is_pending, plaid_connection_id to transactions
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS source VARCHAR(20) NOT NULL DEFAULT 'MANUAL';
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS external_transaction_id VARCHAR(100);
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS is_pending BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS plaid_connection_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_tx_plaid_conn_ext_id ON transactions(plaid_connection_id, external_transaction_id);
