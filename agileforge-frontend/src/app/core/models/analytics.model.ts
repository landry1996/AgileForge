export interface BurndownDataPoint {
  date: string;
  remainingPoints: number;
  idealPoints: number;
}

export interface BurnupDataPoint {
  date: string;
  totalScope: number;
  completedPoints: number;
}

export interface VelocityDataPoint {
  sprintName: string;
  committedPoints: number;
  completedPoints: number;
  sprintNumber: number;
}

export interface SprintMetrics {
  sprintId: string;
  sprintName: string;
  totalTickets: number;
  completedTickets: number;
  totalPoints: number;
  completedPoints: number;
  burndownData: BurndownDataPoint[];
  burnupData: BurnupDataPoint[];
}

export interface ProjectAnalytics {
  projectId: string;
  totalTickets: number;
  openTickets: number;
  closedTickets: number;
  averageVelocity: number;
  averageCycleTimeDays: number;
  velocityHistory: VelocityDataPoint[];
  ticketsByType: Record<string, number>;
  ticketsByPriority: Record<string, number>;
  ticketsByStatus: Record<string, number>;
}

export interface TeamWorkload {
  userId: string;
  userName: string;
  assignedTickets: number;
  inProgressTickets: number;
  completedThisSprint: number;
  totalPointsAssigned: number;
}
