CREATE TABLE missions (
    id UUID PRIMARY KEY,
    emergency_id VARCHAR(255),
    drone_id VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    outcome VARCHAR(50),
    mission_type VARCHAR(50),
    priority VARCHAR(50),
    assigned_at TIMESTAMP,
    unattended_since TIMESTAMP,
    completed_at TIMESTAMP,
    closed_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE drone_projections (
    drone_id VARCHAR(255) PRIMARY KEY,
    status VARCHAR(50),
    type VARCHAR(50),
    lat DOUBLE PRECISION DEFAULT 0,
    lng DOUBLE PRECISION DEFAULT 0,
    battery_percentage INTEGER DEFAULT 100,
    mission_id VARCHAR(255),
    last_updated_at TIMESTAMP
);

CREATE INDEX idx_missions_status ON missions(status);
CREATE INDEX idx_missions_emergency_id ON missions(emergency_id);
CREATE INDEX idx_missions_drone_id ON missions(drone_id);
CREATE INDEX idx_drone_projections_status ON drone_projections(status);
