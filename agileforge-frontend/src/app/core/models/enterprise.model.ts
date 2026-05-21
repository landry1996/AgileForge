export interface AuditEvent {
  id: string;
  organizationId: string;
  projectId: string;
  userId: string;
  action: string;
  entityType: string;
  entityId: string;
  details: any;
  ipAddress: string;
  severity: string;
  createdAt: string;
}

export interface AuditSummary {
  totalEvents: number;
  criticalCount: number;
  todayEvents: number;
  topActions: Record<string, number>;
  topUsers: Record<string, number>;
}

export interface AuditAlertRule {
  id: string;
  name: string;
  actionPattern: string;
  severity: string;
  notifyEmails: string;
  active: boolean;
  createdAt: string;
}

export interface Portfolio {
  id: string;
  organizationId: string;
  name: string;
  description: string;
  ownerId: string;
  createdAt: string;
}

export interface PortfolioDashboard {
  portfolioId: string;
  name: string;
  totalProjects: number;
  overallHealth: number;
  projectSummaries: PortfolioProjectSummary[];
  riskHeatMap: RiskHeatMapEntry[];
}

export interface PortfolioProjectSummary {
  projectId: string;
  name: string;
  key: string;
  healthScore: number;
  activeSprintProgress: number;
  totalTickets: number;
  openTickets: number;
}

export interface RiskHeatMapEntry {
  projectId: string;
  projectName: string;
  riskLevel: string;
  riskScore: number;
}

export interface CapacityEntry {
  id: string;
  projectId: string;
  userId: string;
  userName: string;
  sprintId: string;
  availableHours: number;
  plannedLeaveHours: number;
  notes: string;
  createdAt: string;
}

export interface TeamCapacity {
  projectId: string;
  sprintId: string;
  totalAvailable: number;
  totalLeave: number;
  netCapacity: number;
  members: MemberCapacity[];
}

export interface MemberCapacity {
  userId: string;
  userName: string;
  availableHours: number;
  leaveHours: number;
  assignedPoints: number;
  loadPercentage: number;
}

export interface CapacityForecast {
  projectId: string;
  currentSprintCapacity: number;
  averageVelocity: number;
  estimatedSprints: number;
  bottleneckSkills: string[];
}

export interface Incident {
  id: string;
  projectId: string;
  title: string;
  description: string;
  severity: string;
  status: string;
  commanderId: string;
  startedAt: string;
  resolvedAt: string;
  rootCause: string;
  resolution: string;
  postMortem: string;
  participantIds: string[];
  durationMinutes: number;
  createdAt: string;
}

export interface IncidentEvent {
  id: string;
  incidentId: string;
  userId: string;
  eventType: string;
  message: string;
  createdAt: string;
}

export interface IncidentTimeline {
  incidentId: string;
  title: string;
  severity: string;
  status: string;
  events: IncidentEvent[];
  participants: string[];
  durationMinutes: number;
}

export interface WebhookSubscription {
  id: string;
  projectId: string;
  url: string;
  events: string[];
  active: boolean;
  lastTriggeredAt: string;
  failureCount: number;
  createdAt: string;
}

export interface ApiKeyResponse {
  id: string;
  name: string;
  keyPrefix: string;
  permissions: string[];
  expiresAt: string;
  lastUsedAt: string;
  active: boolean;
  createdAt: string;
}

export interface ApiKeyCreated {
  id: string;
  name: string;
  key: string;
  keyPrefix: string;
}

export interface ClientPortalConfig {
  id: string;
  projectId: string;
  enabled: boolean;
  welcomeMessage: string;
  allowedTicketTypes: string;
  showRoadmap: boolean;
  showReleases: boolean;
  showChangelog: boolean;
  customBranding: any;
  userCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface ClientUser {
  id: string;
  portalId: string;
  email: string;
  name: string;
  company: string;
  active: boolean;
  lastLoginAt: string;
  createdAt: string;
}

export interface ClientFeedback {
  id: string;
  portalId: string;
  ticketId: string;
  clientUserId: string;
  clientName: string;
  type: string;
  content: string;
  rating: number;
  createdAt: string;
}

export interface ClientPortalView {
  projectName: string;
  welcomeMessage: string;
  publicTickets: any[];
  roadmapItems: any[];
  releases: any[];
}
