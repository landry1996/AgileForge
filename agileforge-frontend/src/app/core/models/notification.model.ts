export interface Notification {
  id: string;
  type: NotificationType;
  title: string;
  message: string;
  referenceId?: string;
  referenceType?: string;
  read: boolean;
  createdAt: string;
}

export type NotificationType =
  | 'TICKET_ASSIGNED'
  | 'TICKET_STATUS_CHANGED'
  | 'TICKET_COMMENTED'
  | 'TICKET_MENTIONED'
  | 'SPRINT_STARTED'
  | 'SPRINT_COMPLETED'
  | 'PROJECT_MEMBER_ADDED'
  | 'TICKET_DUE_SOON';

export interface SearchResponse<T> {
  items: T[];
  totalCount: number;
  page: number;
  pageSize: number;
  totalPages: number;
}

export interface SearchFilters {
  q?: string;
  projectId?: string;
  status?: string;
  type?: string;
  priority?: string;
  assigneeId?: string;
  page?: number;
  size?: number;
}
