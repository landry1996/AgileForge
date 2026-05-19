-- Git repository connections per project
CREATE TABLE git_repositories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    provider VARCHAR(20) NOT NULL DEFAULT 'GITHUB',
    owner VARCHAR(100) NOT NULL,
    repo_name VARCHAR(100) NOT NULL,
    default_branch VARCHAR(100) NOT NULL DEFAULT 'main',
    access_token_encrypted VARCHAR(500),
    webhook_secret VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(project_id, owner, repo_name)
);

-- Linked branches
CREATE TABLE git_branches (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    repository_id UUID NOT NULL REFERENCES git_repositories(id) ON DELETE CASCADE,
    ticket_id UUID REFERENCES tickets(id) ON DELETE SET NULL,
    branch_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_git_branches_ticket ON git_branches(ticket_id);

-- Linked pull requests
CREATE TABLE git_pull_requests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    repository_id UUID NOT NULL REFERENCES git_repositories(id) ON DELETE CASCADE,
    ticket_id UUID REFERENCES tickets(id) ON DELETE SET NULL,
    pr_number INT NOT NULL,
    title VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    author VARCHAR(100),
    source_branch VARCHAR(255),
    target_branch VARCHAR(255),
    url VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    merged_at TIMESTAMP WITH TIME ZONE,
    closed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_git_prs_ticket ON git_pull_requests(ticket_id);
CREATE INDEX idx_git_prs_repo ON git_pull_requests(repository_id);

-- CI/CD pipeline status
CREATE TABLE git_pipelines (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    repository_id UUID NOT NULL REFERENCES git_repositories(id) ON DELETE CASCADE,
    ticket_id UUID REFERENCES tickets(id) ON DELETE SET NULL,
    pr_id UUID REFERENCES git_pull_requests(id) ON DELETE CASCADE,
    pipeline_id VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    url VARCHAR(500),
    started_at TIMESTAMP WITH TIME ZONE,
    finished_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_git_pipelines_ticket ON git_pipelines(ticket_id);
