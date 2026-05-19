-- Workflow definitions per project
CREATE TABLE workflows (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    ticket_type VARCHAR(30) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT false,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    UNIQUE(project_id, ticket_type)
);

CREATE INDEX idx_workflows_project ON workflows(project_id) WHERE is_deleted = false;

-- Workflow statuses (columns in the workflow)
CREATE TABLE workflow_statuses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workflow_id UUID NOT NULL REFERENCES workflows(id) ON DELETE CASCADE,
    name VARCHAR(50) NOT NULL,
    category VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    position INT NOT NULL DEFAULT 0,
    color VARCHAR(7),
    UNIQUE(workflow_id, name)
);

-- Allowed transitions between statuses
CREATE TABLE workflow_transitions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workflow_id UUID NOT NULL REFERENCES workflows(id) ON DELETE CASCADE,
    from_status VARCHAR(50) NOT NULL,
    to_status VARCHAR(50) NOT NULL,
    UNIQUE(workflow_id, from_status, to_status)
);

CREATE INDEX idx_workflow_transitions_workflow ON workflow_transitions(workflow_id);
