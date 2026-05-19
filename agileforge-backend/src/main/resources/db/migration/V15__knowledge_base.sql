-- Knowledge entries per project
CREATE TABLE knowledge_entries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    category VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    tags VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_knowledge_project ON knowledge_entries(project_id) WHERE is_active = true;
CREATE INDEX idx_knowledge_category ON knowledge_entries(project_id, category) WHERE is_active = true;
