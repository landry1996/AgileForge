export interface Workflow {
  id: string;
  projectId: string;
  name: string;
  ticketType: string;
  isDefault: boolean;
  statuses: WorkflowStatus[];
  transitions: WorkflowTransition[];
  createdAt: string;
}

export interface WorkflowStatus {
  name: string;
  category: 'TODO' | 'IN_PROGRESS' | 'DONE';
  position: number;
  color?: string;
}

export interface WorkflowTransition {
  fromStatus: string;
  toStatus: string;
}

export interface CreateWorkflowRequest {
  name: string;
  ticketType: string;
  statuses: WorkflowStatus[];
  transitions: WorkflowTransition[];
}
