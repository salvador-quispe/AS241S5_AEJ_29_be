CREATE TABLE IF NOT EXISTS api_requests (
    id           BIGSERIAL PRIMARY KEY,
    request_uuid VARCHAR(100) UNIQUE,
    type         VARCHAR(20) NOT NULL,
    input_data   TEXT,
    result_data  TEXT,
    status       VARCHAR(20) DEFAULT 'pending',
    created_at   TIMESTAMP,
    completed_at TIMESTAMP
);
