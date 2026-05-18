export interface Project {
  id: string;
  organizationId: string;
  name: string;
  key: string;
  description?: string;
  type: string;
  visibility: string;
  status: string;
  startDate?: string;
  endDate?: string;
  leadId?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface Organization {
  id: string;
  name: string;
  slug: string;
  description?: string;
  plan: string;
  createdAt: string;
}
