// V5.0 Ecosystem Models

// Gamification
export interface Badge {
  id: string;
  name: string;
  description: string;
  icon: string;
  category: string;
  criteriaType: string;
  criteriaThreshold: number;
  points: number;
  rarity: 'COMMON' | 'UNCOMMON' | 'RARE' | 'EPIC' | 'LEGENDARY';
}

export interface UserBadge {
  id: string;
  userId: string;
  badgeId: string;
  earnedAt: string;
  badge: Badge;
}

export interface UserStreak {
  id: string;
  userId: string;
  streakType: string;
  currentCount: number;
  longestCount: number;
  lastActivityDate: string;
}

export interface UserXp {
  id: string;
  userId: string;
  domain: string;
  xpPoints: number;
  level: number;
}

export interface GamificationProfile {
  userId: string;
  badges: UserBadge[];
  streaks: UserStreak[];
  xpByDomain: UserXp[];
  totalXp: number;
  level: number;
}

export interface LeaderboardEntry {
  userId: string;
  userName: string;
  points: number;
  rank: number;
  badgeCount: number;
}

export interface Achievement {
  id: string;
  name: string;
  description: string;
  type: string;
  participants: string[];
}

// AI Agents
export interface AiAgent {
  id: string;
  name: string;
  description: string;
  agentType: string;
  capabilities: string[];
  model: string;
  active: boolean;
}

export interface AiAgentTask {
  id: string;
  agentId: string;
  projectId: string;
  taskType: string;
  status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED';
  inputData: string;
  outputData: string;
  executionTimeMs: number;
  createdAt: string;
  completedAt: string;
}

export interface SprintPlanSuggestion {
  suggestedTickets: string[];
  totalPoints: number;
  completionProbability: number;
  reasoning: string;
  risks: string[];
}

export interface RiskReport {
  overallRisk: string;
  items: RiskItem[];
  recommendations: string[];
}

export interface RiskItem {
  title: string;
  severity: string;
  description: string;
  relatedTicketId: string;
}

// Integrations
export interface IntegrationConfig {
  id: string;
  organizationId: string;
  provider: 'SLACK' | 'TEAMS' | 'DISCORD' | 'JIRA';
  enabled: boolean;
  config: Record<string, unknown>;
  webhookUrl: string;
  createdAt: string;
}

export interface ChannelMapping {
  id: string;
  projectId: string;
  channelId: string;
  channelName: string;
  events: string[];
}

export interface JiraImportJob {
  id: string;
  organizationId: string;
  targetProjectId: string;
  jiraUrl: string;
  jiraProjectKey: string;
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';
  totalItems: number;
  importedItems: number;
  failedItems: number;
  startedAt: string;
  completedAt: string;
}

export interface JiraImportPreview {
  totalIssues: number;
  epics: number;
  stories: number;
  tasks: number;
  bugs: number;
  statuses: string[];
  customFields: string[];
}

// Templates & Marketplace
export interface IndustryTemplate {
  id: string;
  name: string;
  description: string;
  industry: string;
  category: string;
  workflowDefinition: string;
  ticketTypes: string;
  boardColumns: string;
  icon: string;
  popularity: number;
  official: boolean;
}

export interface MarketplaceExtension {
  id: string;
  name: string;
  description: string;
  authorId: string;
  category: string;
  version: string;
  iconUrl: string;
  downloads: number;
  rating: number;
  ratingCount: number;
  status: string;
  publishedAt: string;
}

// Real-time Collaboration
export interface CollaborationSession {
  id: string;
  resourceType: string;
  resourceId: string;
  participants: CollaborationParticipant[];
}

export interface CollaborationParticipant {
  userId: string;
  userName: string;
  cursorPosition: string;
  lastSeenAt: string;
  status: 'ACTIVE' | 'IDLE' | 'AWAY';
}

export interface PresenceInfo {
  userId: string;
  status: 'ONLINE' | 'AWAY' | 'BUSY' | 'OFFLINE';
  currentPage: string;
  currentResourceId: string;
  lastHeartbeat: string;
}
