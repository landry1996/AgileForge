-- AgileForge - Initial Schema
-- V1: Users, Organizations, Roles, Permissions

-- ============================================================
-- EXTENSIONS
-- ============================================================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================
-- USERS
-- ============================================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    display_name VARCHAR(200),
    avatar_url VARCHAR(500),
    phone VARCHAR(20),
    timezone VARCHAR(50) DEFAULT 'UTC',
    locale VARCHAR(10) DEFAULT 'fr',
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_email_verified BOOLEAN NOT NULL DEFAULT false,
    email_verified_at TIMESTAMP WITH TIME ZONE,
    last_login_at TIMESTAMP WITH TIME ZONE,
    failed_login_attempts INT DEFAULT 0,
    locked_until TIMESTAMP WITH TIME ZONE,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_active ON users(is_active) WHERE is_deleted = false;

-- ============================================================
-- ORGANIZATIONS (Tenants)
-- ============================================================
CREATE TABLE organizations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(200) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    logo_url VARCHAR(500),
    website VARCHAR(500),
    plan VARCHAR(50) NOT NULL DEFAULT 'FREE',
    max_users INT DEFAULT 5,
    max_projects INT DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX idx_organizations_slug ON organizations(slug);

-- ============================================================
-- ROLES
-- ============================================================
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    is_system BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE
);

-- ============================================================
-- PERMISSIONS
-- ============================================================
CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    module VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- ============================================================
-- ROLE_PERMISSIONS (Many-to-Many)
-- ============================================================
CREATE TABLE role_permissions (
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- ============================================================
-- ORGANIZATION_MEMBERS
-- ============================================================
CREATE TABLE organization_members (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id),
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    is_active BOOLEAN NOT NULL DEFAULT true,
    UNIQUE(organization_id, user_id)
);

CREATE INDEX idx_org_members_org ON organization_members(organization_id);
CREATE INDEX idx_org_members_user ON organization_members(user_id);

-- ============================================================
-- REFRESH TOKENS
-- ============================================================
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT false,
    revoked_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    ip_address VARCHAR(45),
    user_agent VARCHAR(500)
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);

-- ============================================================
-- AUDIT LOG
-- ============================================================
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id),
    organization_id UUID REFERENCES organizations(id),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID,
    old_value JSONB,
    new_value JSONB,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_org ON audit_logs(organization_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_created ON audit_logs(created_at);

-- ============================================================
-- SEED DATA: Default Roles
-- ============================================================
INSERT INTO roles (id, name, code, description, is_system) VALUES
    (uuid_generate_v4(), 'Platform Admin', 'PLATFORM_ADMIN', 'Full platform access', true),
    (uuid_generate_v4(), 'Organization Admin', 'ORG_ADMIN', 'Organization-level admin', true),
    (uuid_generate_v4(), 'Project Manager', 'PROJECT_MANAGER', 'Manages projects and teams', true),
    (uuid_generate_v4(), 'Product Owner', 'PRODUCT_OWNER', 'Manages backlog and priorities', true),
    (uuid_generate_v4(), 'Scrum Master', 'SCRUM_MASTER', 'Facilitates agile ceremonies', true),
    (uuid_generate_v4(), 'Tech Lead', 'TECH_LEAD', 'Technical leadership and architecture', true),
    (uuid_generate_v4(), 'Developer', 'DEVELOPER', 'Development team member', true),
    (uuid_generate_v4(), 'QA Engineer', 'QA_ENGINEER', 'Quality assurance', true),
    (uuid_generate_v4(), 'DevOps Engineer', 'DEVOPS', 'Infrastructure and deployment', true),
    (uuid_generate_v4(), 'Business Analyst', 'BUSINESS_ANALYST', 'Requirements and analysis', true),
    (uuid_generate_v4(), 'Stakeholder', 'STAKEHOLDER', 'View-only business stakeholder', true),
    (uuid_generate_v4(), 'Client', 'CLIENT', 'External client with limited access', true),
    (uuid_generate_v4(), 'Viewer', 'VIEWER', 'Read-only access', true);

-- ============================================================
-- SEED DATA: Default Permissions
-- ============================================================
INSERT INTO permissions (id, name, code, description, module) VALUES
    -- Organization
    (uuid_generate_v4(), 'Create Organization', 'ORG_CREATE', 'Create new organizations', 'ORGANIZATION'),
    (uuid_generate_v4(), 'Update Organization', 'ORG_UPDATE', 'Update organization settings', 'ORGANIZATION'),
    (uuid_generate_v4(), 'Delete Organization', 'ORG_DELETE', 'Delete organization', 'ORGANIZATION'),
    (uuid_generate_v4(), 'Manage Members', 'ORG_MANAGE_MEMBERS', 'Add/remove organization members', 'ORGANIZATION'),
    -- Project
    (uuid_generate_v4(), 'Create Project', 'PROJECT_CREATE', 'Create new projects', 'PROJECT'),
    (uuid_generate_v4(), 'Update Project', 'PROJECT_UPDATE', 'Update project settings', 'PROJECT'),
    (uuid_generate_v4(), 'Delete Project', 'PROJECT_DELETE', 'Delete project', 'PROJECT'),
    (uuid_generate_v4(), 'View Project', 'PROJECT_VIEW', 'View project details', 'PROJECT'),
    -- Ticket
    (uuid_generate_v4(), 'Create Ticket', 'TICKET_CREATE', 'Create tickets', 'TICKET'),
    (uuid_generate_v4(), 'Update Ticket', 'TICKET_UPDATE', 'Update tickets', 'TICKET'),
    (uuid_generate_v4(), 'Delete Ticket', 'TICKET_DELETE', 'Delete tickets', 'TICKET'),
    (uuid_generate_v4(), 'Assign Ticket', 'TICKET_ASSIGN', 'Assign tickets to members', 'TICKET'),
    (uuid_generate_v4(), 'Transition Ticket', 'TICKET_TRANSITION', 'Change ticket status', 'TICKET'),
    -- Sprint
    (uuid_generate_v4(), 'Create Sprint', 'SPRINT_CREATE', 'Create sprints', 'SPRINT'),
    (uuid_generate_v4(), 'Start Sprint', 'SPRINT_START', 'Start a sprint', 'SPRINT'),
    (uuid_generate_v4(), 'Close Sprint', 'SPRINT_CLOSE', 'Close a sprint', 'SPRINT'),
    -- Release
    (uuid_generate_v4(), 'Create Release', 'RELEASE_CREATE', 'Create releases', 'RELEASE'),
    (uuid_generate_v4(), 'Deploy Release', 'RELEASE_DEPLOY', 'Deploy a release', 'RELEASE'),
    -- Admin
    (uuid_generate_v4(), 'Manage Roles', 'ADMIN_MANAGE_ROLES', 'Manage roles and permissions', 'ADMIN'),
    (uuid_generate_v4(), 'View Audit Logs', 'ADMIN_VIEW_AUDIT', 'View audit trail', 'ADMIN'),
    (uuid_generate_v4(), 'Manage Workflows', 'ADMIN_MANAGE_WORKFLOWS', 'Configure workflows', 'ADMIN');
