CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('USER', 'ADMIN')),
    created_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

INSERT INTO users(id, email, password_hash, role, created_at)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'admin@example.com',
    '$2a$10$uzNtMTWMS8UDRHLRo0BbcuA1Dl.JvfGom3MRw8nGPsi4OY9pAQ4xy',
    'ADMIN',
    NOW()
);
