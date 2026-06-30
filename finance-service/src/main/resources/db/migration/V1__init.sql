CREATE TABLE families (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    owner_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE family_members (
    id UUID PRIMARY KEY,
    family_id UUID NOT NULL REFERENCES families(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('OWNER', 'MEMBER')),
    joined_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    UNIQUE (family_id, user_id)
);

CREATE INDEX idx_family_members_user ON family_members(user_id);

CREATE TABLE categories (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    system BOOLEAN NOT NULL,
    owner_id UUID,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE operations (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    family_id UUID REFERENCES families(id) ON DELETE SET NULL,
    category_id UUID NOT NULL REFERENCES categories(id),
    type VARCHAR(20) NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    amount NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    operation_date DATE NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_operations_user_date ON operations(user_id, operation_date);
CREATE INDEX idx_operations_family_date ON operations(family_id, operation_date);

INSERT INTO categories(id, name, type, system, owner_id) VALUES
('10000000-0000-0000-0000-000000000001', 'Продукты', 'EXPENSE', TRUE, NULL),
('10000000-0000-0000-0000-000000000002', 'Транспорт', 'EXPENSE', TRUE, NULL),
('10000000-0000-0000-0000-000000000003', 'Здоровье', 'EXPENSE', TRUE, NULL),
('10000000-0000-0000-0000-000000000004', 'Развлечения', 'EXPENSE', TRUE, NULL),
('10000000-0000-0000-0000-000000000005', 'Зарплата', 'INCOME', TRUE, NULL),
('10000000-0000-0000-0000-000000000006', 'Подарки', 'INCOME', TRUE, NULL);
