CREATE TABLE drones (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'INACTIVE',
    type VARCHAR(50) NOT NULL,
    lat DOUBLE PRECISION NOT NULL DEFAULT 0,
    lng DOUBLE PRECISION NOT NULL DEFAULT 0,
    altitude_meters DOUBLE PRECISION DEFAULT 0,
    battery_percentage INTEGER NOT NULL DEFAULT 100,
    battery_status VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    mission_id VARCHAR(255),
    last_telemetry_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_drones_status ON drones(status);
CREATE INDEX idx_drones_type ON drones(type);

-- Drones de prueba para la demo
INSERT INTO drones (id, name, status, type, lat, lng, battery_percentage, battery_status, last_telemetry_at)
VALUES
  (gen_random_uuid(), 'DR-001', 'AVAILABLE', 'RESCUE', -12.0464, -77.0428, 95, 'NORMAL', NOW()),
  (gen_random_uuid(), 'DR-002', 'AVAILABLE', 'MEDICAL', -12.0550, -77.0350, 88, 'NORMAL', NOW()),
  (gen_random_uuid(), 'DR-003', 'AVAILABLE', 'SEARCH',  -12.0380, -77.0500, 72, 'NORMAL', NOW()),
  (gen_random_uuid(), 'DR-004', 'MAINTENANCE','FIRE',   -12.0460, -77.0420, 100,'NORMAL', NOW());
