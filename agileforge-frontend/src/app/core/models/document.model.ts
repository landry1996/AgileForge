export interface Document {
  id: string;
  projectId: string;
  parentId?: string;
  title: string;
  content?: string;
  docType: DocumentType;
  status: DocumentStatus;
  position: number;
  authorId: string;
  lastEditedBy?: string;
  version: number;
  linkedTicketIds: string[];
  createdAt: string;
  updatedAt?: string;
}

export type DocumentType = 'PAGE' | 'API_DOC' | 'ADR' | 'SPEC' | 'MEETING_NOTES' | 'RUNBOOK' | 'ONBOARDING' | 'FAQ' | 'RELEASE_NOTES' | 'POST_MORTEM';
export type DocumentStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';

export interface DocumentVersion {
  id: string;
  documentId: string;
  title: string;
  version: number;
  editedBy: string;
  changeSummary?: string;
  createdAt: string;
}

export interface DocumentTree {
  id: string;
  title: string;
  docType: DocumentType;
  position: number;
  children: DocumentTree[];
}

export interface CreateDocumentRequest {
  title: string;
  content?: string;
  docType?: DocumentType;
  parentId?: string;
  status?: DocumentStatus;
}

export interface UpdateDocumentRequest {
  title?: string;
  content?: string;
  status?: DocumentStatus;
  changeSummary?: string;
}
