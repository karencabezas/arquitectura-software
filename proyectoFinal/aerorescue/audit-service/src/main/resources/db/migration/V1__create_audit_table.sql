CREATE TABLE audit_entries (
    id UUID PRIMARY KEY,
    event_id VARCHAR(255),
    event_type VARCHAR(100) NOT NULL,
    correlation_id VARCHAR(255),
    source VARCHAR(100),
    payload TEXT,
    occurred_at TIMESTAMP,
    recorded_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_correlation_id ON audit_entries(correlation_id);
CREATE INDEX idx_audit_event_type ON audit_entries(event_type);
CREATE INDEX idx_audit_occurred_at ON audit_entries(occurred_at DESC);
