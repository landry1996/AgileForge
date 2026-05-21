-- V23: External integrations (Slack, Teams, Jira import) + Industry templates

CREATE TABLE integration_configs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    provider VARCHAR(50) NOT NULL,
    enabled BOOLEAN DEFAULT FALSE,
    config JSONB NOT NULL DEFAULT '{}',
    access_token TEXT,
    refresh_token TEXT,
    token_expires_at TIMESTAMP,
    webhook_url TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(organization_id, provider)
);

CREATE TABLE integration_channel_mappings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    integration_id UUID NOT NULL REFERENCES integration_configs(id) ON DELETE CASCADE,
    project_id UUID NOT NULL REFERENCES projects(id),
    channel_id VARCHAR(255) NOT NULL,
    channel_name VARCHAR(255),
    event_types TEXT NOT NULL DEFAULT 'ALL',
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE jira_import_jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    target_project_id UUID REFERENCES projects(id),
    jira_url VARCHAR(500) NOT NULL,
    jira_project_key VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    total_items INT DEFAULT 0,
    imported_items INT DEFAULT 0,
    failed_items INT DEFAULT 0,
    error_log TEXT,
    field_mapping JSONB DEFAULT '{}',
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE industry_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    industry VARCHAR(100) NOT NULL,
    category VARCHAR(100),
    workflow_definition JSONB NOT NULL,
    ticket_types JSONB,
    board_columns JSONB,
    default_labels JSONB,
    sample_tickets JSONB,
    icon VARCHAR(50),
    popularity INT DEFAULT 0,
    is_official BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE marketplace_extensions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    author_id UUID REFERENCES users(id),
    category VARCHAR(100) NOT NULL,
    version VARCHAR(20) NOT NULL DEFAULT '1.0.0',
    icon_url TEXT,
    manifest JSONB NOT NULL,
    downloads INT DEFAULT 0,
    rating NUMERIC(3,2) DEFAULT 0,
    rating_count INT DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    published_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE installed_extensions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    extension_id UUID NOT NULL REFERENCES marketplace_extensions(id),
    config JSONB DEFAULT '{}',
    installed_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(organization_id, extension_id)
);

-- Seed industry templates
INSERT INTO industry_templates (name, description, industry, category, workflow_definition, ticket_types, board_columns, icon) VALUES
('Software Development - Scrum', 'Standard Scrum workflow for software teams', 'SOFTWARE', 'Agile',
 '{"statuses":["BACKLOG","TODO","IN_PROGRESS","CODE_REVIEW","QA","DONE"],"transitions":[{"from":"BACKLOG","to":"TODO"},{"from":"TODO","to":"IN_PROGRESS"},{"from":"IN_PROGRESS","to":"CODE_REVIEW"},{"from":"CODE_REVIEW","to":"QA"},{"from":"QA","to":"DONE"}]}',
 '["EPIC","STORY","TASK","BUG","SPIKE"]',
 '[{"name":"Backlog","status":"BACKLOG"},{"name":"To Do","status":"TODO"},{"name":"In Progress","status":"IN_PROGRESS"},{"name":"Code Review","status":"CODE_REVIEW"},{"name":"QA","status":"QA"},{"name":"Done","status":"DONE"}]',
 'code'),
('Software Development - Kanban', 'Kanban flow for continuous delivery teams', 'SOFTWARE', 'Lean',
 '{"statuses":["BACKLOG","READY","IN_PROGRESS","REVIEW","DONE"],"transitions":[{"from":"BACKLOG","to":"READY"},{"from":"READY","to":"IN_PROGRESS"},{"from":"IN_PROGRESS","to":"REVIEW"},{"from":"REVIEW","to":"DONE"}]}',
 '["TASK","BUG","FEATURE","IMPROVEMENT"]',
 '[{"name":"Backlog","status":"BACKLOG","wipLimit":null},{"name":"Ready","status":"READY","wipLimit":5},{"name":"In Progress","status":"IN_PROGRESS","wipLimit":3},{"name":"Review","status":"REVIEW","wipLimit":2},{"name":"Done","status":"DONE","wipLimit":null}]',
 'columns'),
('Marketing Campaign', 'Campaign management workflow', 'MARKETING', 'Campaign',
 '{"statuses":["IDEATION","PLANNING","CREATION","REVIEW","SCHEDULED","LIVE","COMPLETED"],"transitions":[{"from":"IDEATION","to":"PLANNING"},{"from":"PLANNING","to":"CREATION"},{"from":"CREATION","to":"REVIEW"},{"from":"REVIEW","to":"SCHEDULED"},{"from":"SCHEDULED","to":"LIVE"},{"from":"LIVE","to":"COMPLETED"}]}',
 '["CAMPAIGN","CONTENT","DESIGN","ANALYTICS"]',
 '[{"name":"Ideation","status":"IDEATION"},{"name":"Planning","status":"PLANNING"},{"name":"Creation","status":"CREATION"},{"name":"Review","status":"REVIEW"},{"name":"Scheduled","status":"SCHEDULED"},{"name":"Live","status":"LIVE"},{"name":"Done","status":"COMPLETED"}]',
 'megaphone'),
('DevOps & SRE', 'Infrastructure and reliability workflow', 'DEVOPS', 'Infrastructure',
 '{"statuses":["BACKLOG","DESIGN","IMPLEMENTATION","TESTING","SECURITY_REVIEW","STAGING","PRODUCTION","MONITORING"],"transitions":[{"from":"BACKLOG","to":"DESIGN"},{"from":"DESIGN","to":"IMPLEMENTATION"},{"from":"IMPLEMENTATION","to":"TESTING"},{"from":"TESTING","to":"SECURITY_REVIEW"},{"from":"SECURITY_REVIEW","to":"STAGING"},{"from":"STAGING","to":"PRODUCTION"},{"from":"PRODUCTION","to":"MONITORING"}]}',
 '["INFRASTRUCTURE","AUTOMATION","INCIDENT","CHANGE_REQUEST","SECURITY"]',
 '[{"name":"Backlog","status":"BACKLOG"},{"name":"Design","status":"DESIGN"},{"name":"Implement","status":"IMPLEMENTATION"},{"name":"Test","status":"TESTING"},{"name":"Security","status":"SECURITY_REVIEW"},{"name":"Staging","status":"STAGING"},{"name":"Production","status":"PRODUCTION"}]',
 'server'),
('HR & Recruitment', 'Hiring pipeline management', 'HR', 'Recruitment',
 '{"statuses":["SOURCED","SCREENING","INTERVIEW","TECHNICAL","OFFER","HIRED","REJECTED"],"transitions":[{"from":"SOURCED","to":"SCREENING"},{"from":"SCREENING","to":"INTERVIEW"},{"from":"INTERVIEW","to":"TECHNICAL"},{"from":"TECHNICAL","to":"OFFER"},{"from":"OFFER","to":"HIRED"},{"from":"SCREENING","to":"REJECTED"},{"from":"INTERVIEW","to":"REJECTED"},{"from":"TECHNICAL","to":"REJECTED"}]}',
 '["POSITION","CANDIDATE","ONBOARDING"]',
 '[{"name":"Sourced","status":"SOURCED"},{"name":"Screening","status":"SCREENING"},{"name":"Interview","status":"INTERVIEW"},{"name":"Technical","status":"TECHNICAL"},{"name":"Offer","status":"OFFER"},{"name":"Hired","status":"HIRED"}]',
 'users');

CREATE INDEX idx_integration_configs_org ON integration_configs(organization_id);
CREATE INDEX idx_jira_import_org ON jira_import_jobs(organization_id);
CREATE INDEX idx_industry_templates_industry ON industry_templates(industry);
CREATE INDEX idx_marketplace_ext_category ON marketplace_extensions(category, status);
CREATE INDEX idx_installed_ext_org ON installed_extensions(organization_id);
