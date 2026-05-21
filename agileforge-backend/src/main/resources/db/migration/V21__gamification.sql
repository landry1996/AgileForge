-- V21: Gamification system - badges, achievements, streaks, leaderboard

CREATE TABLE badges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    icon VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    criteria_type VARCHAR(50) NOT NULL,
    criteria_threshold INT NOT NULL DEFAULT 1,
    points INT NOT NULL DEFAULT 10,
    rarity VARCHAR(20) NOT NULL DEFAULT 'COMMON',
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE user_badges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    badge_id UUID NOT NULL REFERENCES badges(id),
    earned_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, badge_id)
);

CREATE TABLE user_streaks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    streak_type VARCHAR(50) NOT NULL,
    current_count INT NOT NULL DEFAULT 0,
    longest_count INT NOT NULL DEFAULT 0,
    last_activity_date DATE,
    started_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, streak_type)
);

CREATE TABLE user_xp (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    domain VARCHAR(50) NOT NULL,
    xp_points INT NOT NULL DEFAULT 0,
    level INT NOT NULL DEFAULT 1,
    updated_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, domain)
);

CREATE TABLE achievements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    project_id UUID REFERENCES projects(id),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    achievement_type VARCHAR(50) NOT NULL,
    achieved_at TIMESTAMP DEFAULT NOW(),
    participants TEXT
);

CREATE TABLE leaderboard_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    period VARCHAR(20) NOT NULL,
    period_start DATE NOT NULL,
    points INT NOT NULL DEFAULT 0,
    rank INT,
    opt_in BOOLEAN DEFAULT FALSE,
    UNIQUE(user_id, organization_id, period, period_start)
);

-- Seed default badges
INSERT INTO badges (name, description, icon, category, criteria_type, criteria_threshold, points, rarity) VALUES
('First Commit', 'Linked your first commit to a ticket', 'git-commit', 'DEVELOPMENT', 'COMMITS', 1, 10, 'COMMON'),
('Bug Squasher', 'Resolved 10 bugs', 'bug', 'DEVELOPMENT', 'BUGS_FIXED', 10, 25, 'COMMON'),
('Bug Exterminator', 'Resolved 50 bugs', 'shield-bug', 'DEVELOPMENT', 'BUGS_FIXED', 50, 100, 'RARE'),
('Sprint Champion', 'Completed all assigned tickets in a sprint', 'trophy', 'AGILE', 'SPRINT_COMPLETE', 1, 50, 'UNCOMMON'),
('Review Master', 'Reviewed 25 pull requests', 'eye', 'COLLABORATION', 'PR_REVIEWS', 25, 50, 'UNCOMMON'),
('Documentation Hero', 'Created 10 documentation pages', 'book', 'DOCUMENTATION', 'DOCS_CREATED', 10, 30, 'COMMON'),
('Test Champion', 'Added tests for 20 tickets', 'check-circle', 'QUALITY', 'TESTS_ADDED', 20, 50, 'UNCOMMON'),
('Velocity King', 'Delivered 100+ story points in one sprint', 'zap', 'AGILE', 'SPRINT_POINTS', 100, 100, 'RARE'),
('Mentor', 'Helped unblock 10 teammates', 'users', 'COLLABORATION', 'UNBLOCKS', 10, 75, 'RARE'),
('Streak Master', 'Maintained a 30-day delivery streak', 'flame', 'CONSISTENCY', 'STREAK_DAYS', 30, 150, 'EPIC');

CREATE INDEX idx_user_badges_user ON user_badges(user_id);
CREATE INDEX idx_user_streaks_user ON user_streaks(user_id);
CREATE INDEX idx_user_xp_user ON user_xp(user_id);
CREATE INDEX idx_leaderboard_org_period ON leaderboard_entries(organization_id, period, period_start);
