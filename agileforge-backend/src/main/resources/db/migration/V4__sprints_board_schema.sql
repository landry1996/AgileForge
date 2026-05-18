-- AgileForge - V4: Sprints and Board

-- ============================================================
-- SPRINTS
-- ============================================================
CREATE TABLE sprints (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    goal TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNING',
    start_date DATE,
    end_date DATE,
    capacity INT,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX idx_sprints_project ON sprints(project_id);
CREATE INDEX idx_sprints_project_status ON sprints(project_id, status) WHERE is_deleted = false;

-- ============================================================
-- BOARD COLUMNS
-- ============================================================
CREATE TABLE board_columns (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    mapped_status VARCHAR(30) NOT NULL,
    position INT NOT NULL DEFAULT 0,
    wip_limit INT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_board_columns_project ON board_columns(project_id);

-- ============================================================
-- Add sprint_id FK to tickets (already has the column from V3)
-- ============================================================
ALTER TABLE tickets ADD CONSTRAINT fk_tickets_sprint
    FOREIGN KEY (sprint_id) REFERENCES sprints(id) ON DELETE SET NULL;
