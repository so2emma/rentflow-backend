CREATE TABLE inbound_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lease_id UUID NOT NULL REFERENCES leases(id) ON DELETE RESTRICT,
    nomba_transaction_id VARCHAR(100) UNIQUE NOT NULL,
    nomba_session_id VARCHAR(100),
    amount DECIMAL(15, 2) NOT NULL,
    sender_name VARCHAR(150),
    sender_bank_name VARCHAR(150),
    sender_account_number VARCHAR(30),
    transaction_time TIMESTAMP WITH TIME ZONE NOT NULL,
    raw_payload TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_inbound_tx_lease ON inbound_transactions(lease_id);
