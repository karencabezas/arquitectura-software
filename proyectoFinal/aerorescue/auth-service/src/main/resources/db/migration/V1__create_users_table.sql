CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    region VARCHAR(100),
    clearance_level INTEGER NOT NULL DEFAULT 1,
    allowed_mission_types TEXT,
    organization_id VARCHAR(100),
    mfa_secret VARCHAR(255),
    mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token VARCHAR(4000) NOT NULL UNIQUE,
    user_id UUID NOT NULL REFERENCES users(id),
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Índices
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);

-- Usuario ADMIN inicial (password: Admin123!)
INSERT INTO users (username, password, role, region, clearance_level, allowed_mission_types, organization_id, mfa_enabled)
VALUES (
    'admin',
    '$2a$12$py.sRBybtZpBbVq/evaDtOceXAnGF599YwwVUtaO/XjOJ5rJyW.nu',
    'ADMIN',
    'ALL',
    5,
    'RESCUE,MEDICAL,SEARCH,FIRE',
    'ORG-001',
    FALSE
);

-- Operador de Lima Norte (password: Operator123!)
INSERT INTO users (username, password, role, region, clearance_level, allowed_mission_types, organization_id, mfa_enabled)
VALUES (
    'operator.lima.norte',
    '$2a$12$py.sRBybtZpBbVq/evaDtOceXAnGF599YwwVUtaO/XjOJ5rJyW.nu',
    'OPERATOR',
    'LIMA_NORTE',
    2,
    'RESCUE,MEDICAL',
    'ORG-001',
    FALSE
);

-- Supervisor (password: Supervisor123!)
INSERT INTO users (username, password, role, region, clearance_level, allowed_mission_types, organization_id, mfa_enabled)
VALUES (
    'supervisor.lima',
    '$2a$12$py.sRBybtZpBbVq/evaDtOceXAnGF599YwwVUtaO/XjOJ5rJyW.nu',
    'SUPERVISOR',
    'LIMA_NORTE',
    3,
    'RESCUE,MEDICAL,SEARCH',
    'ORG-001',
    FALSE
);

-- Técnico de drones (password: Tech123!)
INSERT INTO users (username, password, role, region, clearance_level, allowed_mission_types, organization_id, mfa_enabled)
VALUES (
    'tech.drones',
    '$2a$12$py.sRBybtZpBbVq/evaDtOceXAnGF599YwwVUtaO/XjOJ5rJyW.nu',
    'DRONE_TECH',
    'ALL',
    1,
    'RESCUE,MEDICAL,SEARCH,FIRE',
    'ORG-001',
    FALSE
);
