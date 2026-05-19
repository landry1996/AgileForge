export interface GitRepository {
  id: string;
  projectId: string;
  provider: string;
  owner: string;
  repoName: string;
  defaultBranch: string;
  isActive: boolean;
  createdAt: string;
}

export interface GitBranch {
  id: string;
  repositoryId: string;
  ticketId?: string;
  branchName: string;
  createdAt: string;
}

export interface GitPullRequest {
  id: string;
  repositoryId: string;
  ticketId?: string;
  prNumber: number;
  title: string;
  status: 'OPEN' | 'MERGED' | 'CLOSED';
  author: string;
  sourceBranch: string;
  targetBranch: string;
  url: string;
  createdAt: string;
  mergedAt?: string;
  closedAt?: string;
}

export interface GitPipeline {
  id: string;
  repositoryId: string;
  ticketId?: string;
  status: 'PENDING' | 'RUNNING' | 'SUCCESS' | 'FAILED' | 'CANCELLED';
  url: string;
  startedAt?: string;
  finishedAt?: string;
  createdAt: string;
}

export interface TicketDevInfo {
  ticketId: string;
  branches: GitBranch[];
  pullRequests: GitPullRequest[];
  pipelines: GitPipeline[];
}

export interface ConnectRepositoryRequest {
  owner: string;
  repoName: string;
  defaultBranch?: string;
  accessToken?: string;
}
