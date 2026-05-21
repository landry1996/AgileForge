-- V24: AI Agents - autonomous agents framework

CREATE TABLE ai_agents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    agent_type VARCHAR(50) NOT NULL,
    capabilities JSONB NOT NULL DEFAULT '[]',
    system_prompt TEXT,
    model VARCHAR(50) NOT NULL DEFAULT 'claude-sonnet',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE ai_agent_tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agent_id UUID NOT NULL REFERENCES ai_agents(id),
    project_id UUID REFERENCES projects(id),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    triggered_by UUID REFERENCES users(id),
    task_type VARCHAR(50) NOT NULL,
    input_data JSONB NOT NULL,
    output_data JSONB,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    error_message TEXT,
    execution_time_ms BIGINT,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE ai_agent_schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agent_id UUID NOT NULL REFERENCES ai_agents(id),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    project_id UUID REFERENCES projects(id),
    cron_expression VARCHAR(100) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    last_run_at TIMESTAMP,
    next_run_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Seed default AI agents
INSERT INTO ai_agents (name, description, agent_type, capabilities, system_prompt, model) VALUES
('Sprint Planner', 'Autonomously plans sprints based on velocity, capacity, and priorities', 'SPRINT_PLANNING',
 '["analyze_velocity","check_capacity","prioritize_backlog","suggest_sprint_scope","detect_overload"]',
 'You are an AI Sprint Planning agent. Analyze team velocity, individual capacity, ticket priorities, and dependencies to propose optimal sprint content. Consider historical performance and upcoming absences.',
 'claude-sonnet'),
('Risk Detective', 'Continuously monitors project health and detects risks', 'RISK_DETECTION',
 '["analyze_blockers","predict_delays","detect_scope_creep","monitor_velocity","alert_stakeholders"]',
 'You are an AI Risk Detection agent. Monitor project metrics, identify emerging risks, predict delays, and provide actionable recommendations to prevent issues before they occur.',
 'claude-sonnet'),
('Documentation Agent', 'Auto-generates and updates project documentation', 'DOCUMENTATION',
 '["generate_api_docs","update_changelogs","create_runbooks","summarize_decisions","generate_onboarding"]',
 'You are an AI Documentation agent. Generate clear, comprehensive technical documentation from code changes, ticket resolutions, and team decisions. Keep documentation up-to-date automatically.',
 'claude-sonnet'),
('Code Review Agent', 'Reviews PRs and provides intelligent feedback', 'CODE_REVIEW',
 '["analyze_pr","detect_bugs","suggest_improvements","check_standards","estimate_risk"]',
 'You are an AI Code Review agent. Analyze pull requests for potential bugs, security issues, performance problems, and adherence to project standards. Provide actionable, constructive feedback.',
 'claude-sonnet'),
('Retrospective Agent', 'Generates sprint retrospective insights', 'RETROSPECTIVE',
 '["analyze_sprint_metrics","identify_patterns","suggest_improvements","compare_sprints","celebrate_wins"]',
 'You are an AI Retrospective agent. Analyze sprint data to identify what went well, what needs improvement, and propose specific action items. Track improvement trends across sprints.',
 'claude-haiku');

CREATE INDEX idx_ai_agent_tasks_agent ON ai_agent_tasks(agent_id, status);
CREATE INDEX idx_ai_agent_tasks_project ON ai_agent_tasks(project_id, created_at DESC);
CREATE INDEX idx_ai_agent_schedules_next ON ai_agent_schedules(next_run_at) WHERE enabled = TRUE;
