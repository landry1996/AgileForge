-- AgileForge - V2: Projects and Project Members

-- ============================================================
-- PROJECTS
-- ============================================================
CREATE TABLE projects (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    key VARCHAR(10) NOT NULL,
    description TEXT,
    logo_url VARCHAR(500),
    type VARCHAR(30) NOT NULL DEFAULT 'SOFTWARE',
    visibility VARCHAR(20) NOT NULL DEFAULT 'PRIVATE',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    start_date DATE,
    end_date DATE,
    lead_id UUID REFERENCES users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    UNIQUE(organization_id, key)
);

CREATE INDEX idx_projects_org ON projects(organization_id);
CREATE INDEX idx_projects_key ON projects(organization_id, key);
CREATE INDEX idx_projects_status ON projects(status) WHERE is_deleted = false;
CREATE INDEX idx_projects_lead ON projects(lead_id);

-- ============================================================
-- PROJECT MEMBERS
-- ============================================================
CREATE TABLE project_members (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id),
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    is_active BOOLEAN NOT NULL DEFAULT true,
    UNIQUE(project_id, user_id)
);

CREATE INDEX idx_project_members_project ON project_members(project_id);
CREATE INDEX idx_project_members_user ON project_members(user_id);
