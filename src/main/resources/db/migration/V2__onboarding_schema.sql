-- Landlords Profile (Linked to Users 1-to-1)
CREATE TABLE landlords (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE RESTRICT,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    bank_code VARCHAR(10) NOT NULL,
    bank_account_number VARCHAR(10) NOT NULL,
    bank_account_name VARCHAR(150) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_landlords_user ON landlords(user_id);

-- Properties (Owned by Landlords)
CREATE TABLE properties (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    landlord_id UUID NOT NULL REFERENCES landlords(id) ON DELETE RESTRICT,
    name VARCHAR(150) NOT NULL,
    address TEXT NOT NULL,
    property_code VARCHAR(20) UNIQUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_properties_landlord ON properties(landlord_id);

-- Units within a Property
CREATE TABLE units (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    property_id UUID NOT NULL REFERENCES properties(id) ON DELETE RESTRICT,
    unit_number VARCHAR(20) NOT NULL,
    base_rent DECIMAL(15, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'VACANT', -- VACANT, OCCUPIED, MAINTENANCE
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(property_id, unit_number)
);

-- Tenants Profile (Linked to Users 1-to-1)
CREATE TABLE tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE RESTRICT,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    bvn VARCHAR(11) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Lease Agreements
CREATE TABLE leases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE RESTRICT,
    unit_id UUID NOT NULL REFERENCES units(id) ON DELETE RESTRICT,
    start_date DATE NOT NULL,
    end_date DATE,
    grace_period_days INT DEFAULT 5,
    late_fee_percentage DECIMAL(5, 2) DEFAULT 5.00,
    nomba_vact_ref VARCHAR(64) UNIQUE NOT NULL,
    nomba_vact_number VARCHAR(20) UNIQUE, -- Will be filled in Phase 3
    nomba_vact_bank VARCHAR(100),         -- Will be filled in Phase 3
    deposit_wallet_balance DECIMAL(15, 2) DEFAULT 0.00,
    status VARCHAR(20) DEFAULT 'PENDING_VIRTUAL_ACCOUNT', -- PENDING_VIRTUAL_ACCOUNT, ACTIVE, EXPIRED
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
