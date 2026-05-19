-- Documentation pages
CREATE TABLE documents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    parent_id UUID REFERENCES documents(id),
    title VARCHAR(300) NOT NULL,
    content TEXT,
    doc_type VARCHAR(30) NOT NULL DEFAULT 'PAGE',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    position INT NOT NULL DEFAULT 0,
    author_id UUID NOT NULL REFERENCES users(id),
    last_edited_by UUID REFERENCES users(id),
    version INT NOT NULL DEFAULT 1,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_documents_project ON documents(project_id) WHERE is_deleted = false;
CREATE INDEX idx_documents_parent ON documents(parent_id) WHERE is_deleted = false;

-- Document versions history
CREATE TABLE document_versions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    document_id UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    title VARCHAR(300) NOT NULL,
    content TEXT,
    version INT NOT NULL,
    edited_by UUID NOT NULL REFERENCES users(id),
    change_summary VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_doc_versions_document ON document_versions(document_id);

-- Document-ticket links
CREATE TABLE document_ticket_links (
    document_id UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    ticket_id UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    PRIMARY KEY (document_id, ticket_id)
);
