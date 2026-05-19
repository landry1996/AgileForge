export interface PromptTemplate {
  id: string;
  projectId?: string;
  name: string;
  category: PromptCategory;
  template: string;
  variables?: string;
  isGlobal: boolean;
  usageCount: number;
  rating: number;
  createdAt: string;
}

export type PromptCategory = 'BACKEND' | 'FRONTEND' | 'TESTING' | 'BUG_FIX' | 'REFACTORING' | 'DEVOPS' | 'DOCUMENTATION' | 'SECURITY' | 'PERFORMANCE' | 'CODE_REVIEW' | 'MIGRATION';

export interface GeneratedPrompt {
  id: string;
  ticketId: string;
  promptText: string;
  templateName?: string;
  createdAt: string;
}

export interface GeneratePromptRequest {
  templateId?: string;
  customInstructions?: string;
}
