-- Prompt templates library
CREATE TABLE prompt_templates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID REFERENCES projects(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    template TEXT NOT NULL,
    variables TEXT,
    is_global BOOLEAN NOT NULL DEFAULT false,
    usage_count INT NOT NULL DEFAULT 0,
    rating DOUBLE PRECISION DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_prompt_templates_project ON prompt_templates(project_id);
CREATE INDEX idx_prompt_templates_category ON prompt_templates(category);

-- Generated prompts history
CREATE TABLE generated_prompts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ticket_id UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    template_id UUID REFERENCES prompt_templates(id),
    prompt_text TEXT NOT NULL,
    generated_by UUID NOT NULL REFERENCES users(id),
    rating INT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_generated_prompts_ticket ON generated_prompts(ticket_id);
