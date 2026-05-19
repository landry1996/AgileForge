export interface Release {
  id: string;
  projectId: string;
  name: string;
  version: string;
  description?: string;
  status: ReleaseStatus;
  startDate?: string;
  releaseDate?: string;
  releasedAt?: string;
  ticketCount: number;
  completedCount: number;
  progress: number;
  createdAt: string;
  updatedAt?: string;
}

export type ReleaseStatus = 'PLANNING' | 'IN_PROGRESS' | 'READY' | 'RELEASED' | 'CANCELLED';

export interface ReleaseReadiness {
  releaseId: string;
  version: string;
  totalTickets: number;
  completedTickets: number;
  openBugs: number;
  unresolvedDependencies: number;
  readinessScore: number;
  recommendation: string;
}

export interface RoadmapItem {
  id: string;
  projectId: string;
  title: string;
  description?: string;
  category?: string;
  status: 'PLANNED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
  startDate?: string;
  endDate?: string;
  color?: string;
  position: number;
  releaseId?: string;
  epicId?: string;
  createdAt: string;
}

export interface CreateReleaseRequest {
  name: string;
  version: string;
  description?: string;
  startDate?: string;
  releaseDate?: string;
}

export interface CreateRoadmapItemRequest {
  title: string;
  description?: string;
  category?: string;
  startDate?: string;
  endDate?: string;
  color?: string;
  releaseId?: string;
  epicId?: string;
}
