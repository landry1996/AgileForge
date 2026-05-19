-- Portfolio (grouping of projects)
CREATE TABLE portfolios (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    owner_id UUID REFERENCES users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_portfolios_org ON portfolios(organization_id) WHERE is_deleted = false;

-- Portfolio-project mapping
CREATE TABLE portfolio_projects (
    portfolio_id UUID NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    priority INT NOT NULL DEFAULT 0,
    PRIMARY KEY (portfolio_id, project_id)
);

-- Capacity entries (team availability)
CREATE TABLE capacity_entries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    sprint_id UUID REFERENCES sprints(id),
    available_hours DOUBLE PRECISION NOT NULL,
    planned_leave_hours DOUBLE PRECISION NOT NULL DEFAULT 0,
    notes VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_capacity_project_sprint ON capacity_entries(project_id, sprint_id);
CREATE INDEX idx_capacity_user ON capacity_entries(user_id);
