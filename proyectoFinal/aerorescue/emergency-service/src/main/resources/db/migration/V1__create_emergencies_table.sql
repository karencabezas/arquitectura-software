CREATE TABLE emergencies (
    id UUID PRIMARY KEY,
    status VARCHAR(50) NOT NULL,
    priority VARCHAR(50) NOT NULL,
    type VARCHAR(50) NOT NULL,
    address VARCHAR(500),
    region VARCHAR(100),
    lat DOUBLE PRECISION,
    lng DOUBLE PRECISION,
    description VARCHAR(1000),
    reported_by VARCHAR(255),
    organization_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_emergencies_region ON emergencies(region);
CREATE INDEX idx_emergencies_status ON emergencies(status);
CREATE INDEX idx_emergencies_org ON emergencies(organization_id);
