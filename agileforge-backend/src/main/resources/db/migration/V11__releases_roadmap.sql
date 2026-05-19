-- Releases
CREATE TABLE releases (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    version VARCHAR(50) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNING',
    start_date DATE,
    release_date DATE,
    released_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    UNIQUE(project_id, version)
);

CREATE INDEX idx_releases_project ON releases(project_id) WHERE is_deleted = false;

-- Release-ticket association
CREATE TABLE release_tickets (
    release_id UUID NOT NULL REFERENCES releases(id) ON DELETE CASCADE,
    ticket_id UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    PRIMARY KEY (release_id, ticket_id)
);

-- Roadmap items (milestones)
CREATE TABLE roadmap_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    start_date DATE,
    end_date DATE,
    color VARCHAR(7),
    position INT NOT NULL DEFAULT 0,
    release_id UUID REFERENCES releases(id),
    epic_id UUID REFERENCES tickets(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    is_deleted BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX idx_roadmap_items_project ON roadmap_items(project_id) WHERE is_deleted = false;
