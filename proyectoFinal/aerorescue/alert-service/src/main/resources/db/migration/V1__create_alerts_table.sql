CREATE TABLE alerts (
    id UUID PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    related_entity_type VARCHAR(50),
    related_entity_id VARCHAR(255),
    mission_id VARCHAR(255),
    emergency_id VARCHAR(255),
    message VARCHAR(1000),
    auto_resolvable BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_alerts_type ON alerts(type);
CREATE INDEX idx_alerts_severity ON alerts(severity);
CREATE INDEX idx_alerts_created_at ON alerts(created_at);
