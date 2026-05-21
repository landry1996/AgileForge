-- V22: Real-time collaboration - presence, sessions, collaborative editing

CREATE TABLE collaboration_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resource_type VARCHAR(50) NOT NULL,
    resource_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE collaboration_participants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL REFERENCES collaboration_sessions(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    cursor_position JSONB,
    last_seen_at TIMESTAMP DEFAULT NOW(),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    UNIQUE(session_id, user_id)
);

CREATE TABLE collaboration_operations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL REFERENCES collaboration_sessions(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    operation_type VARCHAR(20) NOT NULL,
    operation_data JSONB NOT NULL,
    vector_clock JSONB,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE user_presence (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ONLINE',
    current_page VARCHAR(255),
    current_resource_id UUID,
    last_heartbeat TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id)
);

CREATE INDEX idx_collab_sessions_resource ON collaboration_sessions(resource_type, resource_id);
CREATE INDEX idx_collab_participants_session ON collaboration_participants(session_id);
CREATE INDEX idx_collab_ops_session ON collaboration_operations(session_id, created_at);
CREATE INDEX idx_user_presence_status ON user_presence(status);
