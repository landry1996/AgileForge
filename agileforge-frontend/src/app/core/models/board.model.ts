import { Ticket } from './ticket.model';

export interface Board {
  projectId: string;
  projectName: string;
  projectKey: string;
  activeSprintId?: string;
  activeSprintName?: string;
  columns: BoardColumn[];
}

export interface BoardColumn {
  id: string;
  name: string;
  mappedStatus: string;
  position: number;
  wipLimit?: number;
  tickets: Ticket[];
}
