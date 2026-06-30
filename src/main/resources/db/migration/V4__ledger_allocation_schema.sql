CREATE TABLE ledger_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lease_id UUID NOT NULL REFERENCES leases(id) ON DELETE RESTRICT,
    entry_type VARCHAR(50) NOT NULL,
    amount_due DECIMAL(15, 2) NOT NULL,
    amount_paid DECIMAL(15, 2) DEFAULT 0.00,
    due_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'UNPAID',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ledger_entries_lease ON ledger_entries(lease_id);

CREATE TABLE payment_allocations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inbound_transaction_id UUID NOT NULL REFERENCES inbound_transactions(id) ON DELETE RESTRICT,
    ledger_entry_id UUID REFERENCES ledger_entries(id) ON DELETE RESTRICT,
    amount_allocated DECIMAL(15, 2) NOT NULL,
    is_deposit_rollover BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_allocations_tx ON payment_allocations(inbound_transaction_id);
CREATE INDEX idx_allocations_entry ON payment_allocations(ledger_entry_id);
