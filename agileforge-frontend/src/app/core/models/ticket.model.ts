export interface Ticket {
  id: string;
  projectId: string;
  fullKey: string;
  key: string;
  number: number;
  title: string;
  description?: string;
  type: TicketType;
  status: TicketStatus;
  priority: TicketPriority;
  assigneeId?: string;
  reporterId: string;
  epicId?: string;
  parentId?: string;
  sprintId?: string;
  storyPoints?: number;
  estimatedHours?: number;
  loggedHours?: number;
  dueDate?: string;
  environment?: string;
  component?: string;
  labels?: string;
  affectedVersion?: string;
  fixVersion?: string;
  qualityScore: number;
  createdAt: string;
  updatedAt?: string;
}

export type TicketType = 'EPIC' | 'STORY' | 'TASK' | 'BUG' | 'SPIKE' | 'SUBTASK' | 'IMPROVEMENT'
  | 'FEATURE' | 'TECH_DEBT' | 'SECURITY' | 'PERFORMANCE' | 'DOCUMENTATION' | 'DESIGN' | 'RESEARCH' | 'DEVOPS';

export type TicketStatus = 'BACKLOG' | 'TODO' | 'IN_PROGRESS' | 'CODE_REVIEW' | 'QA' | 'DONE'
  | 'BLOCKED' | 'CANCELLED' | 'IN_REVIEW' | 'DEPLOYED';

export type TicketPriority = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'TRIVIAL';

export interface CreateTicketRequest {
  title: string;
  description?: string;
  type: TicketType;
  priority: TicketPriority;
  assigneeId?: string;
  epicId?: string;
  parentId?: string;
  storyPoints?: number;
  estimatedHours?: number;
  dueDate?: string;
  environment?: string;
  component?: string;
  labels?: string;
}
