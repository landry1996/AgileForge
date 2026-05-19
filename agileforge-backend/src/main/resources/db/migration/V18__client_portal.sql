-- Client portal configuration per project
CREATE TABLE client_portals (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE UNIQUE,
    is_enabled BOOLEAN NOT NULL DEFAULT false,
    welcome_message TEXT,
    allowed_ticket_types VARCHAR(500),
    show_roadmap BOOLEAN NOT NULL DEFAULT true,
    show_releases BOOLEAN NOT NULL DEFAULT true,
    show_changelog BOOLEAN NOT NULL DEFAULT true,
    custom_branding JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE
);

-- Client users (external users linked to a portal)
CREATE TABLE client_users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    portal_id UUID NOT NULL REFERENCES client_portals(id) ON DELETE CASCADE,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(200) NOT NULL,
    company VARCHAR(200),
    is_active BOOLEAN NOT NULL DEFAULT true,
    last_login_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(portal_id, email)
);

-- Client feedback on tickets
CREATE TABLE client_feedback (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    portal_id UUID NOT NULL REFERENCES client_portals(id) ON DELETE CASCADE,
    ticket_id UUID REFERENCES tickets(id),
    client_user_id UUID NOT NULL REFERENCES client_users(id),
    type VARCHAR(20) NOT NULL DEFAULT 'COMMENT',
    content TEXT NOT NULL,
    rating INT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_client_feedback_portal ON client_feedback(portal_id);
CREATE INDEX idx_client_feedback_ticket ON client_feedback(ticket_id);
