export interface TimeEntry {
  id: string;
  ticketId: string;
  userId: string;
  hours: number;
  description?: string;
  workDate: string;
  createdAt: string;
}

export interface TimeTrackingSummary {
  ticketId: string;
  totalLogged: number;
  estimatedHours?: number;
  entries: TimeEntry[];
}

export interface CreateTimeEntryRequest {
  hours: number;
  description?: string;
  workDate?: string;
}
