export interface Sprint {
  id: string;
  projectId: string;
  name: string;
  goal?: string;
  status: SprintStatus;
  startDate?: string;
  endDate?: string;
  capacity?: number;
  totalTickets: number;
  doneTickets: number;
  totalPoints?: number;
  createdAt: string;
}

export type SprintStatus = 'PLANNING' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED';

export interface CreateSprintRequest {
  name: string;
  goal?: string;
  startDate?: string;
  endDate?: string;
  capacity?: number;
}

export interface SprintMetrics {
  totalTickets: number;
  doneTickets: number;
  totalPoints: number;
  donePoints: number;
}
