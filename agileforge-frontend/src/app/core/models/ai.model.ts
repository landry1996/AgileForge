export interface GeneratedTicket {
  title: string;
  description: string;
  type: string;
  priority: string;
  storyPoints?: number;
  acceptanceCriteria?: string;
}

export interface QualityAnalysis {
  score: number;
  issues: string[];
  suggestions: string[];
  improvedTitle?: string;
  improvedDescription?: string;
}

export interface GenerateTicketsRequest {
  description: string;
  projectContext?: string;
}

export interface GenerateBacklogRequest {
  projectName: string;
  projectDescription: string;
  projectType?: string;
  maxTickets?: number;
}
