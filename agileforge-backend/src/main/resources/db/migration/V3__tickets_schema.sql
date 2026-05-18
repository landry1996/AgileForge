-- AgileForge - V3: Tickets, Comments, History

-- ============================================================
-- TICKETS
-- ============================================================
CREATE TABLE tickets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    key VARCHAR(10) NOT NULL,
    number BIGINT NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    type VARCHAR(30) NOT NULL DEFAULT 'TASK',
    status VARCHAR(30) NOT NULL DEFAULT 'BACKLOG',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    assignee_id UUID REFERENCES users(id),
    reporter_id UUID NOT NULL REFERENCES users(id),
    epic_id UUID REFERENCES tickets(id),
    parent_id UUID REFERENCES tickets(id),
    sprint_id UUID,
    story_points INT,
    estimated_hours DOUBLE PRECISION,
    logged_hours DOUBLE PRECISION DEFAULT 0,
    due_date DATE,
    environment VARCHAR(50),
    component VARCHAR(100),
    labels VARCHAR(500),
    affected_version VARCHAR(50),
    fix_version VARCHAR(50),
    quality_score INT DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    UNIQUE(project_id, number)
);

CREATE INDEX idx_tickets_project ON tickets(project_id);
CREATE INDEX idx_tickets_project_status ON tickets(project_id, status) WHERE is_deleted = false;
CREATE INDEX idx_tickets_assignee ON tickets(assignee_id) WHERE is_deleted = false;
CREATE INDEX idx_tickets_reporter ON tickets(reporter_id);
CREATE INDEX idx_tickets_sprint ON tickets(sprint_id) WHERE is_deleted = false;
CREATE INDEX idx_tickets_epic ON tickets(epic_id) WHERE is_deleted = false;
CREATE INDEX idx_tickets_parent ON tickets(parent_id);
CREATE INDEX idx_tickets_type ON tickets(project_id, type) WHERE is_deleted = false;
CREATE INDEX idx_tickets_priority ON tickets(project_id, priority) WHERE is_deleted = false;
CREATE INDEX idx_tickets_due_date ON tickets(due_date) WHERE is_deleted = false AND due_date IS NOT NULL;

-- Sequence per project for ticket numbers
CREATE SEQUENCE IF NOT EXISTS ticket_number_seq START 1;

-- ============================================================
-- TICKET COMMENTS
-- ============================================================
CREATE TABLE ticket_comments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ticket_id UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    author_id UUID NOT NULL REFERENCES users(id),
    content TEXT NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_ticket_comments_ticket ON ticket_comments(ticket_id) WHERE is_deleted = false;

-- ============================================================
-- TICKET HISTORY (audit trail per ticket)
-- ============================================================
CREATE TABLE ticket_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ticket_id UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    field VARCHAR(100) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ticket_history_ticket ON ticket_history(ticket_id);
CREATE INDEX idx_ticket_history_created ON ticket_history(created_at);

-- ============================================================
-- TICKET LINKS (dependencies)
-- ============================================================
CREATE TABLE ticket_links (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    source_ticket_id UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    target_ticket_id UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    link_type VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    UNIQUE(source_ticket_id, target_ticket_id, link_type)
);

CREATE INDEX idx_ticket_links_source ON ticket_links(source_ticket_id);
CREATE INDEX idx_ticket_links_target ON ticket_links(target_ticket_id);
