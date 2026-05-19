export interface KnowledgeEntry {
  id: string;
  projectId: string;
  category: KnowledgeCategory;
  title: string;
  content: string;
  tags?: string;
  isActive: boolean;
  createdBy?: string;
  createdAt: string;
  updatedAt?: string;
}

export type KnowledgeCategory = 'TECH_STACK' | 'ARCHITECTURE' | 'CONVENTIONS' | 'DECISIONS' | 'API_ENDPOINTS' | 'BUSINESS_RULES' | 'CONSTRAINTS' | 'PATTERNS' | 'KNOWN_ISSUES' | 'DEPENDENCIES' | 'TEAM_PREFERENCES';

export interface ProjectContext {
  projectId: string;
  projectName: string;
  techStack: string[];
  architecture: string;
  conventions: string[];
  decisions: string[];
  knownIssues: string[];
}

export interface CreateKnowledgeEntryRequest {
  category: KnowledgeCategory;
  title: string;
  content: string;
  tags?: string;
}
